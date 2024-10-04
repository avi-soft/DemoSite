package com.community.api.endpoint.avisoft.controller.notification;

import com.community.api.services.NotificationService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.CustomerDoesNotExistsException;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import java.io.IOException;

@RestController
@RequestMapping("notification")
public class NotificationController
{
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    ExceptionHandlingImplement exceptionHandlingImplement;
    @Autowired
    ResponseService responseService;


    @PostMapping("/notify/{customerId}")
    public ResponseEntity<?> notifyCustomer(@PathVariable Long customerId) throws Exception {
        try
        {
            notificationService.notifyCustomer(customerId);

        }
        catch (CustomerDoesNotExistsException customerDoesNotExistsException)
        {
            ResponseService.generateErrorResponse("Customer does not exist", HttpStatus.NOT_FOUND);
        }catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (RuntimeException e)
        {
            ResponseService.generateErrorResponse("Email address of customer is null. Please add email address ", HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            ResponseService.generateErrorResponse("Something went wrong", HttpStatus.BAD_REQUEST);
        }
        return responseService.generateResponse(HttpStatus.OK,"Notification is sent",customerId);
    }
}
