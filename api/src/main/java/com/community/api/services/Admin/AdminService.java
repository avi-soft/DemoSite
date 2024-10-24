package com.community.api.services.Admin;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomAdmin;
import com.community.api.services.*;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.exception.ExceptionHandlingImplement;
import io.github.bucket4j.Bucket;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService
{
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    ServiceProviderServiceImpl serviceProviderService;
    @Autowired
    private CustomCustomerService customCustomerService;
    @Autowired
    private CustomerService customerService;
    @Value("${twilio.accountSid}")
    private String accountSid;
    @Value("${twilio.authToken}")
    private String authToken;
    @Autowired
    private TwilioServiceForAdmin twilioService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ResponseService responseService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private SkillService skillService;
    @Autowired
    private ServiceProviderInfraService serviceProviderInfraService;
    @Autowired
    private ServiceProviderLanguageService serviceProviderLanguageService;
    @Autowired
    private RateLimiterService rateLimiterService;
    @Autowired
    private SharedUtilityService sharedUtilityService;
    public CustomAdmin findAdminByPhone(String mobile_number, String countryCode) {

        return entityManager.createQuery(Constant.PHONE_QUERY_ADMIN, CustomAdmin.class)
                .setParameter("mobileNumber", mobile_number)
                .setParameter("country_code", countryCode)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public ResponseEntity<?> sendOtpForAdmin(String mobileNumber, String countryCode, HttpSession session) throws UnsupportedEncodingException {
        try {
            mobileNumber = mobileNumber.startsWith("0")
                    ? mobileNumber.substring(1)
                    : mobileNumber;
            if (countryCode == null)
                countryCode = Constant.COUNTRY_CODE;
            Bucket bucket = rateLimiterService.resolveBucket(mobileNumber, "/admin/otp/send-otp");
            if (bucket.tryConsume(1)) {
                if (!serviceProviderService.isValidMobileNumber(mobileNumber)) {
                    return responseService.generateErrorResponse("Invalid mobile number", HttpStatus.BAD_REQUEST);

                }
                ResponseEntity<?> otpResponse = twilioService.sendOtpToMobileForAdmin(mobileNumber, countryCode);
                return otpResponse;
            } else {
                return responseService.generateErrorResponse("You can send OTP only once in 1 minute", HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);

            }

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public ResponseEntity<?> verifyOtpForAdmin(Map<String, Object> adminDetails, HttpSession session, HttpServletRequest request) {
        try {
            String username = (String) adminDetails.get("username");
            String otpEntered = (String) adminDetails.get("otpEntered");
            String mobileNumber = (String) adminDetails.get("mobileNumber");
            String countryCode = (String) adminDetails.get("countryCode");
            Integer role = (Integer) adminDetails.get("role");
            if (countryCode == null || countryCode.isEmpty()) {
                countryCode = Constant.COUNTRY_CODE;
            }

            if (username != null) {
                CustomAdmin customAdmin = findAdminByUsername(username);
                if (customAdmin == null) {
                    return responseService.generateErrorResponse("No records found ", HttpStatus.NOT_FOUND);

                }
                mobileNumber = customAdmin.getMobileNumber();
            }
            else if (mobileNumber == null || mobileNumber.isEmpty()) {

//                if (mobileNumber == null || mobileNumber.isEmpty()) {
                return responseService.generateErrorResponse("mobile number can not be null ", HttpStatus.BAD_REQUEST);

            }

            if (!serviceProviderService.isValidMobileNumber(mobileNumber)) {
                return responseService.generateErrorResponse("Invalid mobile number ", HttpStatus.BAD_REQUEST);

            }
            if (mobileNumber.startsWith("0"))
                mobileNumber = mobileNumber.substring(1);
            CustomAdmin existingAdmin = findAdminByPhone(mobileNumber, countryCode);

            if (existingAdmin == null) {
                return responseService.generateErrorResponse("Invalid Data Provided ", HttpStatus.UNAUTHORIZED);

            }

            String storedOtp = existingAdmin.getOtp();
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            String tokenKey = "authTokenAdmin_" + mobileNumber;


            if (otpEntered == null || otpEntered.trim().isEmpty()) {
                return responseService.generateErrorResponse("OTP cannot be empty", HttpStatus.BAD_REQUEST);
            }
            if (otpEntered.equals(storedOtp)) {
                existingAdmin.setOtp(null);
                entityManager.merge(existingAdmin);


                String existingToken = existingAdmin.getToken();
                Map<String,Object> serviceProviderResponse= sharedUtilityService.adminDetailsMap(existingAdmin);

                if (existingToken != null && jwtUtil.validateToken(existingToken, ipAddress, userAgent)) {


                    Map<String, Object> responseBody = createAuthResponseForAdmin(existingToken, serviceProviderResponse).getBody();


                    return ResponseEntity.ok(responseBody);
                } else {
                    String newToken = jwtUtil.generateToken(existingAdmin.getAdmin_id(), role, ipAddress, userAgent);
//                    session.setAttribute(tokenKey, newToken);

                    existingAdmin.setToken(newToken);
                    entityManager.persist(existingAdmin);
                    Map<String, Object> responseBody = createAuthResponseForAdmin(newToken, serviceProviderResponse).getBody();
                    if(existingAdmin.getSignedUp()==0) {
                        existingAdmin.setSignedUp(1);
                        entityManager.merge(existingAdmin);
                        responseBody.put("message", "User has been signed up");
                    }
                    return ResponseEntity.ok(responseBody);
                }
            } else {
                return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.UNAUTHORIZED);

            }

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Otp verification error" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<Map<String, Object>> createAuthResponseForAdmin(String token, Map<String,Object> adminEntity) {
        Map<String, Object> responseBody = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
            data.put("customAdminDetails",adminEntity);
        responseBody.put("status_code", HttpStatus.OK.value());
        responseBody.put("data", data);
        responseBody.put("token", token);
        responseBody.put("message", "User has been logged in");
        responseBody.put("status", "OK");

        return ResponseEntity.ok(responseBody);
    }

    public ResponseEntity<?> loginWithPasswordForAdmin(@RequestBody Map<String, Object> customAdminDetails, HttpServletRequest request, HttpSession session) {
        try {
            String mobileNumber = (String) customAdminDetails.get("mobileNumber");
            if(mobileNumber!=null) {
                if (mobileNumber.startsWith("0"))
                    mobileNumber = mobileNumber.substring(1);
            }

            String username = (String) customAdminDetails.get("username");
            String password = (String) customAdminDetails.get("password");
            String countryCode = (String) customAdminDetails.getOrDefault("countryCode", Constant.COUNTRY_CODE);
            // Check for empty password
            if (password == null || password.isEmpty()) {
                return responseService.generateErrorResponse("Password cannot be empty", HttpStatus.BAD_REQUEST);

            }
            if (mobileNumber != null && !mobileNumber.isEmpty()) {
                return authenticateByPhone(mobileNumber, countryCode, password, request, session);
            } else if (username != null && !username.isEmpty()) {
                return authenticateByUsername(username, password, request, session);
            } else {
                return responseService.generateErrorResponse("Empty Phone Number or username", HttpStatus.BAD_REQUEST);

            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> authenticateByPhone(String mobileNumber, String countryCode, String password, HttpServletRequest request, HttpSession session) {
        CustomAdmin existingAdmin = findAdminByPhone(mobileNumber, countryCode);
        return validateAdmin(existingAdmin, password, request, session);
    }

    //find service provider by username and validate the password.
    public ResponseEntity<?> authenticateByUsername(String username, String password, HttpServletRequest request, HttpSession session) {
        CustomAdmin existingCustomAdmin = findAdminByUsername(username);
        return validateAdmin(existingCustomAdmin, password, request, session);
    }

    public CustomAdmin findAdminByUsername(String username) {

        return entityManager.createQuery(Constant.USERNAME_QUERY_CUSTOM_ADMIN, CustomAdmin.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public ResponseEntity<?> validateAdmin(CustomAdmin customAdmin, String password, HttpServletRequest request, HttpSession session) {
        if (customAdmin == null) {
            return responseService.generateErrorResponse("No Records Found", HttpStatus.NOT_FOUND);
        }
        if (passwordEncoder.matches(password, customAdmin.getPassword())) {
            System.out.println("inside the match");
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            String tokenKey = "authTokenAdmin_" + customAdmin.getMobileNumber();


            String existingToken = customAdmin.getToken();

            Map<String,Object> adminResponse= sharedUtilityService.adminDetailsMap(customAdmin);


            if(existingToken != null && jwtUtil.validateToken(existingToken, ipAddress, userAgent)) {

                Map<String, Object> responseBody = createAuthResponseForAdmin(existingToken, adminResponse).getBody();

                return ResponseEntity.ok(responseBody);
            } else {
                String newToken = jwtUtil.generateToken(customAdmin.getAdmin_id(), customAdmin.getRole(), ipAddress, userAgent);
                session.setAttribute(tokenKey, newToken);

                customAdmin.setToken(newToken);
                entityManager.persist(customAdmin);

                Map<String, Object> responseBody = createAuthResponseForAdmin(newToken, adminResponse).getBody();


                return ResponseEntity.ok(responseBody);


            }
        } else {
            return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
        }
    }

}
