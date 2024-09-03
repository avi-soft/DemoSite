package com.community.api.endpoint.avisoft.controller.otpmodule;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.endpoint.serviceProvider.ServiceProviderStatus;
import com.community.api.entity.CustomCustomer;
import com.community.api.services.*;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import io.github.bucket4j.Bucket;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/otp")
public class OtpEndpoint {

    private static final Logger log = LoggerFactory.getLogger(OtpEndpoint.class);

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    private TwilioService twilioService;

    @Autowired
    private CustomCustomerService customCustomerService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private EntityManager em;

    @Autowired
    private CustomerService customerService;


    @Autowired
    private ServiceProviderServiceImpl serviceProviderService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ResponseService responseService;

    @Value("${twilio.authToken}")
    private String authToken;

    @Value("${twilio.accountSid}")
    private String accountSid;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody CustomCustomer customerDetails, HttpSession session) throws UnsupportedEncodingException {
        try {
            if (customerDetails.getMobileNumber() == null || customerDetails.getMobileNumber().isEmpty()) {
                return responseService.generateErrorResponse(ApiConstants.MOBILE_NUMBER_NULL_OR_EMPTY, HttpStatus.NOT_ACCEPTABLE);
            }

            String mobileNumber = customerDetails.getMobileNumber().startsWith("0")
                    ? customerDetails.getMobileNumber().substring(1)
                    : customerDetails.getMobileNumber();

            String countryCode = customerDetails.getCountryCode() == null || customerDetails.getCountryCode().isEmpty()
                    ? Constant.COUNTRY_CODE
                    : customerDetails.getCountryCode();

            CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhoneWithOtp(customerDetails.getMobileNumber(), countryCode);
            if (existingCustomer != null) {
                return responseService.generateErrorResponse(ApiConstants.CUSTOMER_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
            }

            Bucket bucket = rateLimiterService.resolveBucket(customerDetails.getMobileNumber(), "/otp/send-otp");
            if (bucket.tryConsume(1)) {
                if (!customCustomerService.isValidMobileNumber(mobileNumber)) {
                    return responseService.generateErrorResponse(ApiConstants.INVALID_MOBILE_NUMBER, HttpStatus.BAD_REQUEST);

                }

                ResponseEntity<Map<String, Object>> otpResponse = twilioService.sendOtpToMobile(mobileNumber, countryCode);
                Map<String, Object> responseBody = otpResponse.getBody();

                if (responseBody.get("otp")!=null) {
                    return responseService.generateSuccessResponse((String) responseBody.get("message"), responseBody.get("otp"), HttpStatus.OK);
                } else {
                    return responseService.generateErrorResponse((String) responseBody.get("message"), HttpStatus.BAD_REQUEST);
                }
            } else {
                return responseService.generateErrorResponse(ApiConstants.RATE_LIMIT_EXCEEDED, HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error occurred" + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody Map<String, Object> loginDetails, HttpSession session, HttpServletRequest request) {
        try {
            if (loginDetails == null) {
                return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }

            String otpEntered = (String) loginDetails.get("otpEntered");
            Integer role = (Integer) loginDetails.get("role");
            String countryCode = (String) loginDetails.get("countryCode");
            String username = (String) loginDetails.get("username");
            String mobileNumber = (String) loginDetails.get("mobileNumber");

            if (role == null) {
                return responseService.generateErrorResponse(ApiConstants.ROLE_EMPTY, HttpStatus.BAD_REQUEST);
            }

            if (roleService.findRoleName(role).equals(Constant.roleUser)) {
                if (username != null) {
                    if (customerService == null) {
                        return responseService.generateErrorResponse(ApiConstants.CUSTOMER_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                    Customer customer = customerService.readCustomerByUsername(username);

                    if (customer == null) {
                        return responseService.generateErrorResponse(ApiConstants.NO_RECORDS_FOUND, HttpStatus.INTERNAL_SERVER_ERROR);
                    }

                    CustomCustomer customCustomer = em.find(CustomCustomer.class, customer.getId());
                    if (customCustomer != null) {
                        mobileNumber = customCustomer.getMobileNumber();
                    } else {
                        return responseService.generateErrorResponse(ApiConstants.NO_RECORDS_FOUND, HttpStatus.NOT_FOUND);
                    }
                } else if (mobileNumber == null) {
                    return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.INTERNAL_SERVER_ERROR);
                }

                if (otpEntered == null || otpEntered.trim().isEmpty()) {
                    return responseService.generateErrorResponse("otp is null ", HttpStatus.BAD_REQUEST);
                }

                CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhone(mobileNumber, countryCode);

                if (existingCustomer == null) {
                    return responseService.generateErrorResponse(ApiConstants.NO_RECORDS_FOUND, HttpStatus.NOT_FOUND);
                }

                String storedOtp = existingCustomer.getOtp();
                String ipAddress = request.getRemoteAddr();
                String userAgent = request.getHeader("User-Agent");
                String tokenKey = "authToken_" + mobileNumber;
                Customer customer = customerService.readCustomerById(existingCustomer.getId());

                if (otpEntered.equals(storedOtp)) {
                    existingCustomer.setOtp(null);
                    em.persist(existingCustomer);

                    String existingToken = (String) session.getAttribute(tokenKey);

                    if (existingToken != null && jwtUtil.validateToken(existingToken, ipAddress, userAgent)) {
                        ApiResponse response = new ApiResponse(existingToken, customer, HttpStatus.OK.value(), HttpStatus.OK.name(),"User has been logged in");
                        return ResponseEntity.ok(response);

                    } else {
                        String newToken = jwtUtil.generateToken(existingCustomer.getId(), role, ipAddress, userAgent);
                        session.setAttribute(tokenKey, newToken);
                        ApiResponse response = new ApiResponse(newToken, customer, HttpStatus.OK.value(), HttpStatus.OK.name(),"User has been logged in");
                        return ResponseEntity.ok(response);

                    }
                } else {
                    return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.UNAUTHORIZED);
                }
            } else if (roleService.findRoleName(role).equals(Constant.roleServiceProvider)) {
                return serviceProviderService.verifyOtp(loginDetails, session, request);
            } else {
                return responseService.generateErrorResponse(ApiConstants.INVALID_ROLE, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Otp verification error" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PostMapping("/service-provider-signup")
    public ResponseEntity<?> sendOtpToMobile(@RequestBody Map<String, Object> signupDetails) {
        try {
            String mobileNumber = (String) signupDetails.get("mobileNumber");
            String countryCode = (String) signupDetails.get("countryCode");

            mobileNumber = mobileNumber.startsWith("0") ? mobileNumber.substring(1) : mobileNumber;
            if (customCustomerService.findCustomCustomerByPhone(mobileNumber, countryCode) != null) {
                return responseService.generateErrorResponse(ApiConstants.NUMBER_REGISTERED_AS_CUSTOMER, HttpStatus.BAD_REQUEST);
            }

            if (countryCode == null || countryCode.isEmpty()) {
                countryCode = Constant.COUNTRY_CODE;
            }

            if (!serviceProviderService.isValidMobileNumber(mobileNumber)) {
                return responseService.generateErrorResponse(ApiConstants.INVALID_MOBILE_NUMBER, HttpStatus.BAD_REQUEST);
            }

            Twilio.init(accountSid, authToken);
            String otp = serviceProviderService.generateOTP();

            ServiceProviderEntity existingServiceProvider = serviceProviderService.findServiceProviderByPhone(mobileNumber, countryCode);

            if (existingServiceProvider == null) {
                ServiceProviderEntity serviceProviderEntity = new ServiceProviderEntity();
                serviceProviderEntity.setCountry_code(countryCode);
                serviceProviderEntity.setMobileNumber(mobileNumber);
                serviceProviderEntity.setOtp(otp);
                ServiceProviderStatus serviceProviderStatus = em.find(ServiceProviderStatus.class, Constant.INITIAL_STATUS);
                serviceProviderEntity.setStatus(serviceProviderStatus);
                serviceProviderEntity.setRole(4);
                em.persist(serviceProviderEntity);
            } else if (existingServiceProvider.getOtp() != null) {
                existingServiceProvider.setOtp(otp);
                em.merge(existingServiceProvider);
            } else {
                return responseService.generateErrorResponse(ApiConstants.MOBILE_NUMBER_REGISTERED, HttpStatus.BAD_REQUEST);
            }
            Map<String, Object> details = new HashMap<>();
            String maskedNumber = twilioService.genereateMaskednumber(mobileNumber);
            details.put("otp", otp);
            return responseService.generateSuccessResponse(ApiConstants.OTP_SENT_SUCCESSFULLY + " on " +maskedNumber, details, HttpStatus.OK);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return responseService.generateErrorResponse(ApiConstants.UNAUTHORIZED_ACCESS , HttpStatus.UNAUTHORIZED);
            } else {
                exceptionHandling.handleHttpClientErrorException(e);
                return responseService.generateErrorResponse(ApiConstants.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (ApiException e) {
            exceptionHandling.handleApiException(e);
            return responseService.generateErrorResponse(ApiConstants.ERROR_SENDING_OTP + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.ERROR_SENDING_OTP + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get-service-provider")
    public ResponseEntity<?> getServiceProviderById(@RequestParam Long userId) {
        try {
            ServiceProviderEntity serviceProviderEntity = serviceProviderService.getServiceProviderById(userId);
            if (serviceProviderEntity == null) {
                return responseService.generateErrorResponse("Service provider not found " + userId, HttpStatus.BAD_REQUEST);
            }
            Map<String, Object> details = new HashMap<>();
            // details.put("message", "Service provider details are");
            details.put("status", ApiConstants.STATUS_SUCCESS);
            details.put("status_code", HttpStatus.OK);
            details.put("data", serviceProviderEntity);
            return responseService.generateSuccessResponse("Service provider details are", details, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.INTERNAL_SERVER_ERROR + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    public static class ApiResponse {
        private Data data;
        private int status_code;
        private String status;
        private String message;
        private String token;


        public ApiResponse(String token, Customer customer, int statusCodeValue, String statusCode, String message) {
            this.data = new Data(customer);
            this.status_code = statusCodeValue;
            this.status = statusCode;
            this.message = message;
            this.token = token;
        }

        public Data getData() {
            return data;
        }

        public int getStatus_code() {
            return status_code;
        }

        public String getStatus() {
            return status;
        }

        public String getToken() {
            return token;
        }

        public String getMessage() {
            return message;
        }

        public  class Data {
            private Customer userDetails;

            public Data(Customer customer) {
                this.userDetails = customer;
            }

            public Customer getUserDetails() {
                return userDetails;
            }
        }
    }


}
