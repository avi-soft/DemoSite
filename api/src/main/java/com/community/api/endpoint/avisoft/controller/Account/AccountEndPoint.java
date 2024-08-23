package com.community.api.endpoint.avisoft.controller.Account;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.endpoint.avisoft.controller.Customer.CustomerEndpoint;
import com.community.api.entity.CustomCustomer;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.RoleService;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.TwilioService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import io.swagger.models.auth.In;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNumeric;

@RestController
@RequestMapping(value = "/account",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class AccountEndPoint {
    private CustomerService customerService;
    private JwtUtil jwtUtil;
    private ExceptionHandlingImplement exceptionHandling;
    private EntityManager em;
    private TwilioService twilioService;
    private CustomCustomerService customCustomerService;
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleService roleService;
    @Autowired
    private ServiceProviderServiceImpl serviceProviderService;


    @Autowired
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Autowired
    public void setExceptionHandling(ExceptionHandlingImplement exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }

    @Autowired
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    @Autowired
    public void setTwilioService(TwilioService twilioService) {
        this.twilioService = twilioService;
    }

    @Autowired
    public void setCustomCustomerService(CustomCustomerService customCustomerService) {
        this.customCustomerService = customCustomerService;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }


    @PostMapping("/loginWithOtp")
    @ResponseBody
    public ResponseEntity<?> verifyAndLogin(@RequestBody Map<String,Object>loginDetails, HttpSession session) {
        try {
            String mobileNumber = (String) loginDetails.get("mobileNumber");
            if (mobileNumber != null) {
                if (customCustomerService.isValidMobileNumber(mobileNumber) && isNumeric(mobileNumber)) {
                    return loginWithPhoneOtp(loginDetails, session);
                } else {
                    return ResponseEntity.badRequest().body("Mobile number is not valid");
                }
            } else {
                return loginWithUsernameOtp(loginDetails, session);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Some issue in login: " + e.getMessage());
        }
    }


    @PostMapping("/loginWithPassword")
    @ResponseBody
    public ResponseEntity<?> loginWithPassword(@RequestBody Map<String,Object>loginDetails, HttpSession session,HttpServletRequest request) {
        try {
                String mobileNumber = (String) loginDetails.get("mobileNumber");
                String username = (String) loginDetails.get("username");
                if(mobileNumber!=null)
                {
                if (customCustomerService.isValidMobileNumber(mobileNumber) && isNumeric(mobileNumber)) {
                    return loginWithCustomerPassword(loginDetails, session, request);
                } else {
                    return ResponseEntity.badRequest().body("Mobile number is not valid");
                }
            } else if(username!=null) {
                return loginWithUsername(loginDetails, session,request);
            }
                else
                    return new ResponseEntity<>("Invalid request", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Some issue in login: " + e.getMessage());
        }
    }
    @RequestMapping(value = "phone-otp", method = RequestMethod.POST)
    private ResponseEntity<String> loginWithPhoneOtp(@RequestBody Map<String,Object> loginDetails, HttpSession session) throws UnsupportedEncodingException, UnsupportedEncodingException {
        try {
            if (loginDetails == null) {
                return new ResponseEntity<>("Login details cannot be null", HttpStatus.BAD_REQUEST);
            }
            String mobileNumber = (String) loginDetails.get("mobileNumber");
            String countryCode = (String) loginDetails.get("countryCode");
            Integer role = (Integer) loginDetails.get("role");
            if(mobileNumber==null)
            {
                return new ResponseEntity<>("Mobile number cannot be empty",HttpStatus.BAD_REQUEST);
            }else if(role==null) {
                return new ResponseEntity<>("role cannot be empty", HttpStatus.BAD_REQUEST);
            }
            if (countryCode == null || countryCode.isEmpty()) {
                countryCode = Constant.COUNTRY_CODE;
            }
            String updated_mobile = mobileNumber;
            if (mobileNumber.startsWith("0")) {
                updated_mobile = mobileNumber.substring(1);
            }
            if(roleService.findRoleName(role).equals(Constant.roleUser)) {
                CustomCustomer customerRecords = customCustomerService.findCustomCustomerByPhone(mobileNumber, countryCode);
                if (customerRecords == null) {
                    return new ResponseEntity<>("No Records found", HttpStatus.NOT_FOUND);
                }
                if (customerService == null) {
                    return new ResponseEntity<>("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                Customer customer = customerService.readCustomerById(customerRecords.getId());
                if (customer != null) {
                    twilioService.sendOtpToMobile(updated_mobile, countryCode);

                    String storedOtp = customerRecords.getOtp();
                    return new ResponseEntity<>("OTP Sent on " + mobileNumber + " storedOtp is " + storedOtp, HttpStatus.OK);
                } else {
                    return ResponseEntity.badRequest().body("Mobile number not found");
                }
            } else if (roleService.findRoleName(role).equals(Constant.roleServiceProvider)) {
                if(serviceProviderService.findServiceProviderByPhone(mobileNumber,countryCode)!=null)
                {
                    if(serviceProviderService.findServiceProviderByPhone(mobileNumber,countryCode).getOtp()!=null)
                        return new ResponseEntity<>("Number not registered",HttpStatus.NOT_FOUND);
                    return serviceProviderService.sendOtp(mobileNumber, countryCode, session);
                }
                else return
                new ResponseEntity<>("No records found",HttpStatus.NOT_FOUND);
            }
            else
                return new ResponseEntity<>("Role not specified",HttpStatus.BAD_REQUEST);
        }
       catch (Exception e) {
                exceptionHandling.handleException(e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Some issue in login: " + e.getMessage());
            }
    }

    @Transactional
    @RequestMapping(value = "login-with-username", method = RequestMethod.POST)
    public ResponseEntity<?> loginWithUsername(@RequestBody Map<String,Object> loginDetails, HttpSession session ,HttpServletRequest request) {
        try {
            if (loginDetails == null) {
                return new ResponseEntity<>("Login details cannot be null", HttpStatus.BAD_REQUEST);
            }
            String username = (String) loginDetails.get("username");
            String password = (String) loginDetails.get("password");
            Integer role = (Integer) loginDetails.get("role");
            if(username==null||password==null||role==null)
            {
                return new ResponseEntity<>("username/password number cannot be empty",HttpStatus.BAD_REQUEST);
            }
            if(roleService.findRoleName(role).equals(Constant.roleUser))
            {
            if (customerService == null) {
                return new ResponseEntity<>("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Customer customer = customerService.readCustomerByUsername(username);
            if (customer == null) {
                return new ResponseEntity<>("No records found for the provided username.", HttpStatus.NOT_FOUND);
            }
            CustomCustomer customCustomer=em.find(CustomCustomer.class,customer.getId());
            if (passwordEncoder.matches(password, customer.getPassword())) {

                String tokenKey = "authToken_" + customCustomer.getMobileNumber();
                String existingToken = (String) session.getAttribute(tokenKey);
                String ipAddress = request.getRemoteAddr();
                String userAgent = request.getHeader("User-Agent");
                if (existingToken != null && jwtUtil.validateToken(existingToken,  ipAddress, userAgent)) {

                    return ResponseEntity.ok(CustomerEndpoint.createAuthResponse(existingToken, customer));
                } else {

                    String token = jwtUtil.generateToken(customer.getId(), role,ipAddress,userAgent);
                    session.setAttribute(tokenKey, token);
                    return ResponseEntity.ok(CustomerEndpoint.createAuthResponse(token,customer));

                }
            } else {
                return ResponseEntity.badRequest().body("Invalid password");
            }
        }
        else if(roleService.findRoleName(role).equals(Constant.roleServiceProvider))
            {
                return serviceProviderService.loginWithPassword(loginDetails,session);
            }
        else {
            return new ResponseEntity<>("Invalid role",HttpStatus.BAD_REQUEST);
            }
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Some issue in login: " + e.getMessage());
        }
    }
    @RequestMapping(value = "username-otp", method = RequestMethod.POST)
    private ResponseEntity<?> loginWithUsernameOtp(
            @RequestBody Map<String,Object>loginDetails,HttpSession session) {
        try {
            if (loginDetails == null) {
                return new ResponseEntity<>("Login details cannot be null", HttpStatus.BAD_REQUEST);
            }
            String username = (String) loginDetails.get("username");
            Integer role = (Integer) loginDetails.get("role");
            System.out.println(username);
            if(username==null||role==null)
            {
                return new ResponseEntity<>("username number cannot be empty",HttpStatus.BAD_REQUEST);
            }
            if (customerService == null) {
                return new ResponseEntity<>("customerService is null ", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if(roleService.findRoleName(role).equals(Constant.roleUser)) {
                Customer customer = customerService.readCustomerByUsername(username);
                if (customer == null) {
                    return new ResponseEntity<>("No records found for the provided username.", HttpStatus.NOT_FOUND);
                }
                CustomCustomer customCustomer = em.find(CustomCustomer.class, customer.getId());
                if (customCustomer != null) {
                    return twilioService.sendOtpToMobile(customCustomer.getMobileNumber(), Constant.COUNTRY_CODE);
                } else {
                    return new ResponseEntity<>("No records found", HttpStatus.NO_CONTENT);
                }
            }
            else if(roleService.findRoleName(role).equals(Constant.roleServiceProvider))
            {
                return serviceProviderService.loginWithUsernameAndOTP(username,session);
            }
            else
            {
                return new ResponseEntity<>("Invalid role provided",HttpStatus.BAD_REQUEST);
            }
        }catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Some Error in login", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @RequestMapping(value = "login-with-password", method = RequestMethod.POST)
    public ResponseEntity<?> loginWithCustomerPassword(@RequestBody Map<String,Object>loginDetails, HttpSession session,
                                                       HttpServletRequest request) {
        try {
            if (loginDetails == null) {
                return new ResponseEntity<>("Login details cannot be null", HttpStatus.BAD_REQUEST);
            }
            String mobileNumber = (String) loginDetails.get("mobileNumber");
            String password = (String) loginDetails.get("password");
            String countryCode=(String)loginDetails.get("countryCode");
            Integer role = (Integer)loginDetails.get("role");

            if(mobileNumber==null||password==null||role==null)
            {
                return new ResponseEntity<>("number/password number cannot be empty",HttpStatus.BAD_REQUEST);
            }
            if (countryCode == null) {
                countryCode=Constant.COUNTRY_CODE;
            }
            if (customerService == null) {
                return new ResponseEntity<>("customerService is null", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            /*if (customerDetails.getMobileNumber() == null || customerDetails.getPassword() == null) {
                return new ResponseEntity<>("Invalid mobile number or password", HttpStatus.BAD_REQUEST);
            }*/
            if(roleService.findRoleName(role).equals(Constant.roleUser))
            {
            CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhone(mobileNumber, countryCode);
            if (existingCustomer != null) {
                Customer customer = customerService.readCustomerById(existingCustomer.getId());
                if (passwordEncoder.matches(password, existingCustomer.getPassword())) {
                    String tokenKey = "authToken_" + mobileNumber;
                    String existingToken = (String) session.getAttribute(tokenKey);
                    String ipAddress = request.getRemoteAddr();
                    String userAgent = request.getHeader("User-Agent");
                    if (existingToken != null && jwtUtil.validateToken(existingToken,  ipAddress, userAgent)) {

                        return ResponseEntity.ok(CustomerEndpoint.createAuthResponse(existingToken, customer));
                    } else {

                       String token = jwtUtil.generateToken(existingCustomer.getId(), role,ipAddress,userAgent);
                        session.setAttribute(tokenKey, token);
                        return ResponseEntity.ok(CustomerEndpoint.createAuthResponse(token,customer));

                    }

                } else {
                    return new ResponseEntity<>("Incorrect Password", HttpStatus.UNAUTHORIZED);
                }
            } else {
                return new ResponseEntity<>("Customer does not exist", HttpStatus.NOT_FOUND);
            }
        }
            else if(roleService.findRoleName(role).equals(Constant.roleServiceProvider))
            {
                return serviceProviderService.loginWithPassword(loginDetails,session);
            }
            else return new ResponseEntity<>("Invalid role specified",HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error retrieving Customer", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
