package com.community.api.endpoint.avisoft.controller.otpmodule;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomCustomer;
import com.community.api.endpoint.customer.CustomerDTO;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.RateLimiterService;
import com.community.api.services.RoleService;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.services.TwilioService;
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
import org.springframework.web.bind.annotation.*;
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

    @Value("${twilio.authToken}")
    private String authToken;

    @Value("${twilio.accountSid}")
    private String accountSid;


    @PostMapping("/send-otp")
    public ResponseEntity<String> sendtOtp(@RequestBody CustomCustomer customerDetails, HttpSession session) throws UnsupportedEncodingException {

        try {
            if (customerDetails.getMobileNumber().isEmpty() || customerDetails.getMobileNumber() == null)
                return new ResponseEntity<>("Enter mobile number", HttpStatus.UNPROCESSABLE_ENTITY);

            String mobileNumber = null;
            if (customerDetails.getMobileNumber().startsWith("0")) {
                mobileNumber = customerDetails.getMobileNumber().substring(1);
            } else {
                mobileNumber = customerDetails.getMobileNumber();
            }

            String countryCode = null;
            if (customerDetails.getCountryCode() == null || customerDetails.getCountryCode().isEmpty()) {

                countryCode = Constant.COUNTRY_CODE;
            } else {
                countryCode = customerDetails.getCountryCode();

            }
            CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhoneWithOtp(customerDetails.getMobileNumber(), countryCode);
            twilioService.setotp(mobileNumber, countryCode);

            if(existingCustomer!=null){
                return ResponseEntity.badRequest().body("Customer already exists ");
            }
            Bucket bucket = rateLimiterService.resolveBucket(customerDetails.getMobileNumber(),"/otp/send-otp");
            if (bucket.tryConsume(1)) {
                if (!customCustomerService.isValidMobileNumber(mobileNumber)) {
                    return ResponseEntity.badRequest().body("Invalid mobile number");
                }

                ResponseEntity<String> otpResponse = twilioService.sendOtpToMobile(mobileNumber, countryCode);
                return otpResponse;
            } else {

                ResponseEntity<String>  otpResponse =  ResponseEntity.ok("You can send otp only once in 1 minute" );
                return  otpResponse;
            }


        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error sending OTP: " + e.getMessage());

        }

    }

    @Transactional
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody Map<String,Object> loginDetails, HttpSession session,
                                       HttpServletRequest request) {
        try {
            if (loginDetails == null) {
                return new ResponseEntity<>("Login details cannot be null", HttpStatus.BAD_REQUEST);
            }
            String otpEntered=(String) loginDetails.get("otpEntered");
            Integer role=(Integer) loginDetails.get("role");
            String countryCode=(String) loginDetails.get("countryCode");
            String username=(String) loginDetails.get("username");
            String mobileNumber=(String)loginDetails.get("mobileNumber");
            /*if (customerDetails.getMobileNumber() != null) {

                customerDetails.setMobileNumber(customerDetails.getMobileNumber());

            } else*/
            if(roleService.findRoleName(role).equals(Constant.roleUser))
            {
            if (username != null) {
                if (customerService == null) {
                    return new ResponseEntity<>("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                Customer customer = customerService.readCustomerByUsername(username);

                if (customer == null) {
                    return new ResponseEntity<>("No records found",HttpStatus.NOT_FOUND);
                }
                CustomCustomer customCustomer = em.find(CustomCustomer.class, customer.getId());
                if (customCustomer != null) {
                    mobileNumber=customCustomer.getMobileNumber();
                } else {
                    return new ResponseEntity<>("No records found", HttpStatus.NO_CONTENT);
                }
            } else if(mobileNumber==null) {
                return new ResponseEntity<>("Invalid data", HttpStatus.INTERNAL_SERVER_ERROR);
            }



            if (otpEntered == null || otpEntered.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("OTP cannot be empty");
            }

            CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhone(mobileNumber, countryCode);

            if (existingCustomer == null) {
                return new ResponseEntity<>("No records found for the provided mobile number.", HttpStatus.NOT_FOUND);
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
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("OTP may be deleted send otp again");
            }
        }
        else if(roleService.findRoleName(role).equals(Constant.roleServiceProvider))
            {
                return serviceProviderService.verifyOtp(loginDetails,session,request);
            }
        else
            {
                return new ResponseEntity<>("Invalid role defined",HttpStatus.BAD_REQUEST);
            }
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error verifying OTP", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<AuthResponse> createAuthResponse(String token, Customer customer) {
        AuthResponse authResponse = new AuthResponse(token, customer);
        return ResponseEntity.ok(authResponse);
    }
    @PostMapping("/service-provider-signup")
    @javax.transaction.Transactional
    public ResponseEntity<String> sendOtpToMobile(@RequestBody Map<String, Object> signupDetails) {
        try {
            String mobileNumber = (String) signupDetails.get("mobileNumber");
            String countryCode = (String) signupDetails.get("countryCode");

            if (mobileNumber == null || mobileNumber.isEmpty()) {
                throw new IllegalArgumentException("Mobile number cannot be null or empty");
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
                serviceProviderEntity.setRole(4);//4 corresponds to service provider
                entityManager.persist(serviceProviderEntity);
            } else {
                // Existing entity, use merge
                existingServiceProvider.setOtp(otp);
                entityManager.merge(existingServiceProvider);
            }

            return ResponseEntity.ok("OTP has been sent successfully " + otp);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access: Please check your API key");
            } else {
                exceptionHandling.handleHttpClientErrorException(e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error occurred");
            }
        } catch (ApiException e) {
            exceptionHandling.handleApiException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error sending OTP: " + e.getMessage());
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error sending OTP: " + e.getMessage());
        }
    }
    @GetMapping("getServiceProivider")
    public ResponseEntity<?> getServiceProviderById(@RequestParam Long userId) throws Exception {
        try {
            ServiceProviderEntity serviceProviderEntity = serviceProviderService.getServiceProviderById(userId);
            if (serviceProviderEntity == null) {
                throw new Exception("ServiceProvider with ID " + userId + " not found");
            }
            return ResponseEntity.ok(serviceProviderEntity);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Some fetching account " + e.getMessage());
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
