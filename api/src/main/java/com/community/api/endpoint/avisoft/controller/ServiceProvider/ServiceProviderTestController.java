package com.community.api.endpoint.avisoft.controller.ServiceProvider;

import com.community.api.dto.SubmitTextDto;
import com.community.api.entity.ServiceProviderTest;
import com.community.api.services.ResponseService;
import com.community.api.services.ServiceProviderTestService;
import com.community.api.services.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;

@RestController
@RequestMapping("service-provider-test")
public class ServiceProviderTestController {
    private ServiceProviderTestService testService;
    protected ExceptionHandlingImplement exceptionHandling;
    private ResponseService responseService;

    public ServiceProviderTestController(ServiceProviderTestService testService, ResponseService responseService,ExceptionHandlingImplement exceptionHandling) {
        this.testService = testService;
        this.responseService = responseService;
        this.exceptionHandling = exceptionHandling;
    }

    @PostMapping("/start/{serviceProviderId}")
    public ResponseEntity<?> startTest(@PathVariable Long serviceProviderId) throws  EntityDoesNotExistsException {
        ServiceProviderTest test = testService.startTest(serviceProviderId);
        return responseService.generateResponse(HttpStatus.OK,"Test started",test);
    }

    @PostMapping("/{serviceProviderId}/{testId}/upload-resized-image")
    public ResponseEntity<?> uploadResizedImage(@PathVariable Long serviceProviderId,@PathVariable Long testId, @RequestParam("resizedImage") MultipartFile resizedImage) throws Exception {
        ServiceProviderTest test = testService.uploadResizedImage(serviceProviderId,testId, resizedImage);
        return responseService.generateResponse(HttpStatus.OK,"Image is uploaded",test);
    }

    @PostMapping("/{serviceProviderId}/{testId}/submit-text")
    public ResponseEntity<?> submitTypedText(@PathVariable Long serviceProviderId,@PathVariable Long testId, @RequestBody SubmitTextDto submitTextDto) throws Exception {
        ServiceProviderTest test = testService.submitTypedText(serviceProviderId,testId, submitTextDto.getTypedText());
        return responseService.generateResponse(HttpStatus.OK,"Text is submitted",test);
    }

    @PostMapping("/{serviceProviderId}/{testId}/upload-resized-signature")
    public ResponseEntity<?> uploadResizedSignature(@PathVariable Long serviceProviderId,@PathVariable Long testId, @RequestParam("resizedSignature") MultipartFile resizedSignature) throws Exception {
        ServiceProviderTest test = testService.uploadSignatureImage(serviceProviderId,testId, resizedSignature);
        return responseService.generateResponse(HttpStatus.OK,"Signature image is uploaded",test);
    }

    @GetMapping("/{serviceProviderId}/getAll")
    public ResponseEntity<?> getAllTests(@PathVariable Long serviceProviderId) throws EntityNotFoundException, RuntimeException, EntityDoesNotExistsException, CustomerDoesNotExistsException {
        return responseService.generateSuccessResponse("All tests are found", testService.getServiceProviderTestByServiceProviderId(serviceProviderId), HttpStatus.OK);
    }

    @ExceptionHandler({ EntityDoesNotExistsException.class, EntityNotFoundException.class, RuntimeException.class,IllegalArgumentException.class, Exception.class})
    public ResponseEntity<?> handleException(Exception e) {
        HttpStatus status;
        String message;

        if (e instanceof EntityNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            message = "Test does not exist";
        }

        else if (e instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
            message = e.getMessage();
        }
        else if (e instanceof EntityDoesNotExistsException) {
            status = HttpStatus.BAD_REQUEST;
            message = "Service Provider does not exist";
        }
        else if (e instanceof Exception) {
            status = HttpStatus.BAD_REQUEST;
            message = "";
        }
        else if(e instanceof RuntimeException)
        {
            status= HttpStatus.OK;
            message= "Service Provider test list is empty";
        }
        else {
            status = HttpStatus.BAD_REQUEST;
            message = "Some error occurred";
        }

        exceptionHandling.handleException(e);
        return responseService.generateErrorResponse(message + ": " + e.getMessage(), status);
    }
}
