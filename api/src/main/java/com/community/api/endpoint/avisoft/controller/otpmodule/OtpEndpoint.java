package com.community.api.endpoint.avisoft.controller.otpmodule;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.endpoint.serviceProvider.ServiceProviderStatus;
import com.community.api.entity.CustomCustomer;
import com.community.api.endpoint.customer.CustomerDTO;
import com.community.api.services.*;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import io.github.bucket4j.Bucket;
import io.swagger.models.auth.In;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.HttpClientErrorException;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;

import java.util.Map;


@RestController
@RequestMapping("/otp")
public class OtpEndpoint {

    private static final Logger log = LoggerFactory.getLogger(OtpEndpoint.class);
    private final Logger logger = LoggerFactory.getLogger(OtpEndpoint.class);
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
    private EntityManager entityManager;

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
                return responseService.generateErrorResponse("Enter mobile number", HttpStatus.NOT_ACCEPTABLE);
            }

            String mobileNumber = customerDetails.getMobileNumber().startsWith("0")
                    ? customerDetails.getMobileNumber().substring(1)
                    : customerDetails.getMobileNumber();

            String countryCode = customerDetails.getCountryCode() == null || customerDetails.getCountryCode().isEmpty()
                    ? Constant.COUNTRY_CODE
                    : customerDetails.getCountryCode();

            CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhoneWithOtp(customerDetails.getMobileNumber(), countryCode);
            if (existingCustomer != null) {
                return responseService.generateErrorResponse("Customer already exists", HttpStatus.BAD_REQUEST);
            }

            Bucket bucket = rateLimiterService.resolveBucket(customerDetails.getMobileNumber(), "/otp/send-otp");
            if (bucket.tryConsume(1)) {
                if (!customCustomerService.isValidMobileNumber(mobileNumber)) {
                    return ResponseEntity.badRequest().body("Invalid mobile number");
                }

                ResponseEntity<Map<String, Object>> otpResponse = twilioService.sendOtpToMobile(mobileNumber, countryCode);
                Map<String, Object> responseBody = otpResponse.getBody();

                if ("success".equals(responseBody.get("status"))) {
                    return responseService.generateSuccessResponse((String) responseBody.get("message"), responseBody, HttpStatus.OK);
                } else {
                    return responseService.generateErrorResponse((String) responseBody.get("message"), HttpStatus.BAD_REQUEST);
                }
            } else {
                return responseService.generateErrorResponse("You can send OTP only once in 1 minute", HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error sending OTP: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @Transactional
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody Map<String,Object> loginDetails, HttpSession session,
                                       HttpServletRequest request) {
        try {
            if (loginDetails == null) {
                return responseService.generateErrorResponse("Login details cannot be null ",HttpStatus.BAD_REQUEST);
            }
            String otpEntered=(String) loginDetails.get("otpEntered");
            Integer role=(Integer) loginDetails.get("role");
            String countryCode=(String) loginDetails.get("countryCode");
            String username=(String) loginDetails.get("username");
            String mobileNumber=(String)loginDetails.get("mobileNumber");
            /*if (customerDetails.getMobileNumber() != null) {

                customerDetails.setMobileNumber(customerDetails.getMobileNumber());

            } else*/
            if(role==null)
            {
                return responseService.generateErrorResponse("Role cannot be empty",HttpStatus.BAD_REQUEST);
            }
            if(roleService.findRoleName(role).equals(Constant.roleUser))
            {
            if (username != null) {
                if (customerService == null) {
                    return responseService.generateErrorResponse("Customer service is not initialized",HttpStatus.INTERNAL_SERVER_ERROR);
                }
                Customer customer = customerService.readCustomerByUsername(username);

                if (customer == null) {
                    return responseService.generateErrorResponse("No records found ",HttpStatus.INTERNAL_SERVER_ERROR);

                }
                CustomCustomer customCustomer = em.find(CustomCustomer.class, customer.getId());
                if (customCustomer != null) {
                    mobileNumber=customCustomer.getMobileNumber();
                } else {
                    return responseService.generateErrorResponse("No records found ",HttpStatus.NO_CONTENT);

                }
            } else if(mobileNumber==null) {
                return responseService.generateErrorResponse("Invalid data ",HttpStatus.INTERNAL_SERVER_ERROR);

            }



            if (otpEntered == null || otpEntered.trim().isEmpty()) {
                return responseService.generateErrorResponse("OTP cannot be empty ",HttpStatus.BAD_REQUEST);

            }

            CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhone(mobileNumber, countryCode);

            if (existingCustomer == null) {
                return responseService.generateErrorResponse("No records found for the provided mobile number. ",HttpStatus.NOT_FOUND);
            }


            existingCustomer = customCustomerService.findCustomCustomerByPhone(mobileNumber, countryCode);


            String storedOtp = existingCustomer.getOtp();
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            String tokenKey = "authToken_" + mobileNumber;
            Customer customer = customerService.readCustomerById(existingCustomer.getId());

            if (otpEntered.equals(storedOtp) && otpEntered!=null) {
                existingCustomer.setOtp(null);
                em.persist(existingCustomer);
                String existingToken = (String) session.getAttribute(tokenKey);

                if (existingToken != null && jwtUtil.validateToken(existingToken, ipAddress, userAgent)) {
                    return ResponseEntity.ok(createAuthResponse(existingToken, customer));
                } else {
                    String newToken = jwtUtil.generateToken(existingCustomer.getId(), role, ipAddress, userAgent);
                    session.setAttribute(tokenKey, newToken);
                    return ResponseEntity.ok(createAuthResponse(newToken, customer));
                }
            } else {
                return responseService.generateErrorResponse("send otp again  ",HttpStatus.UNAUTHORIZED);

            }
        }
        else if(roleService.findRoleName(role).equals(Constant.roleServiceProvider))
            {
                return serviceProviderService.verifyOtp(loginDetails,session,request);
            }
        else
            {
                return responseService.generateErrorResponse("Invalid role defined ",HttpStatus.BAD_REQUEST);

            }
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error verifying OTP ",HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    private ResponseEntity<?> createAuthResponse(String token, Customer customer) {
        AuthResponse authResponse = new AuthResponse(token, customer);
        return responseService.generateSuccessResponse("Token details ",authResponse,HttpStatus.OK);

    }
    @Transactional
    @PostMapping("/serviceProviderSignup")
    public ResponseEntity<?> sendOtpToMobile(@RequestBody Map<String, Object> signupDetails) {
        try {
            String mobileNumber = (String) signupDetails.get("mobileNumber");
            String countryCode = (String) signupDetails.get("countryCode");
            mobileNumber = mobileNumber.startsWith("0")
                    ? mobileNumber.substring(1)
                    : mobileNumber;
            if(customCustomerService.findCustomCustomerByPhone(mobileNumber,countryCode)!=null){
                return responseService.generateErrorResponse("Number Already registered as Customer ",HttpStatus.BAD_REQUEST);

            }
            if(countryCode==null)
                countryCode=Constant.COUNTRY_CODE;
            if(!serviceProviderService.isValidMobileNumber(mobileNumber)){
                return responseService.generateErrorResponse("Invalid mobile number ",HttpStatus.BAD_REQUEST);

            }
            if (mobileNumber == null || mobileNumber.isEmpty()) {
                return responseService.generateErrorResponse("Invalid mobile number ",HttpStatus.BAD_REQUEST);
            }

            if (countryCode == null || countryCode.isEmpty()) {
                countryCode = Constant.COUNTRY_CODE;
            }

            Twilio.init(accountSid, authToken);
            String completeMobileNumber = countryCode + mobileNumber;
            String otp = serviceProviderService.generateOTP();

            ServiceProviderEntity existingServiceProvider = serviceProviderService.findServiceProviderByPhone(mobileNumber, countryCode);

            if (existingServiceProvider == null) {
                // New entity, use persist
                ServiceProviderEntity serviceProviderEntity = new ServiceProviderEntity();
                serviceProviderEntity.setCountry_code(countryCode);
                serviceProviderEntity.setMobileNumber(mobileNumber);
                serviceProviderEntity.setOtp(otp);
                ServiceProviderStatus serviceProviderStatus=entityManager.find(ServiceProviderStatus.class,Constant.INITIAL_STATUS);
                serviceProviderEntity.setStatus(serviceProviderStatus);//initial status
                serviceProviderEntity.setRole(4);//4 corresponds to service provider
                entityManager.persist(serviceProviderEntity);
            }
            else if(existingServiceProvider.getOtp()!=null)
            {
                existingServiceProvider.setOtp(otp);
                entityManager.merge(existingServiceProvider);
            }
                if(existingServiceProvider!=null && existingServiceProvider.getOtp()==null) {
                    return responseService.generateErrorResponse("Mobile Number Already Registred ",HttpStatus.BAD_REQUEST);
                }
            return responseService.generateSuccessResponse("OTP has been sent successfully ",null,HttpStatus.OK);


        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return responseService.generateErrorResponse("Unauthorized access: Please check your API key ",HttpStatus.UNAUTHORIZED);

            } else {
                exceptionHandling.handleHttpClientErrorException(e);
                return responseService.generateErrorResponse("Internal server error occurred ",HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (ApiException e) {
            exceptionHandling.handleApiException(e);
            return responseService.generateErrorResponse("Error sending OTP: " + e.getMessage(),HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error sending OTP: " + e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("getServiceProivider")
    public ResponseEntity<?> getServiceProviderById(@RequestParam Long userId) throws Exception {
        try {
            ServiceProviderEntity serviceProviderEntity = serviceProviderService.getServiceProviderById(userId);
            if (serviceProviderEntity == null) {
                return responseService.generateErrorResponse("ServiceProvider with ID " + userId + " not found",HttpStatus.BAD_REQUEST);

            }
            return ResponseEntity.ok(serviceProviderEntity);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some fetching account " + e.getMessage(),HttpStatus.BAD_REQUEST);

        }
    }
    public static class AuthResponse {

        private String token;
        private Customer userDetails;

        public AuthResponse(String token, Customer userDetails) {
            this.token = token;
            this.userDetails = userDetails;
        }

        public String getToken() {
            return token;
        }

        public Customer getUserDetails() {
            return userDetails;
        }
    }
}
