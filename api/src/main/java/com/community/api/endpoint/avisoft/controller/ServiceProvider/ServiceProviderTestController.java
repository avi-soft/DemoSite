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
import java.util.List;

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
        try
        {
            ServiceProviderTest test = testService.startTest(serviceProviderId);
            return responseService.generateResponse(HttpStatus.OK,"Test started",test);
        }
        catch (EntityDoesNotExistsException e)
        {
            return ResponseService.generateErrorResponse("Service Provider does not exist",HttpStatus.NOT_FOUND);
        }
        catch (Exception e)
        {
            return ResponseService.generateErrorResponse("Something went wrong",HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{serviceProviderId}/{testId}/upload-resized-image")
    public ResponseEntity<?> uploadResizedImage(@PathVariable Long serviceProviderId,@PathVariable Long testId, @RequestParam("resizedImage") MultipartFile resizedImage) throws Exception {
        try
        {
            ServiceProviderTest test = testService.uploadResizedImage(serviceProviderId,testId, resizedImage);
            return responseService.generateResponse(HttpStatus.OK,"Image is uploaded",test);
        }
        catch (EntityDoesNotExistsException e)
        {
            return ResponseService.generateErrorResponse("Service Provider does not exist",HttpStatus.NOT_FOUND);
        }
        catch (EntityNotFoundException e)
        {
            return ResponseService.generateErrorResponse("Test does not exist",HttpStatus.NOT_FOUND);
        }
        catch (IllegalArgumentException e)
        {
            return ResponseService.generateErrorResponse(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        catch (Exception e)
        {
            return ResponseService.generateErrorResponse("Something went wrong",HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{serviceProviderId}/{testId}/submit-text")
    public ResponseEntity<?> submitTypedText(@PathVariable Long serviceProviderId,@PathVariable Long testId, @RequestBody SubmitTextDto submitTextDto) throws Exception {
        try
        {
            ServiceProviderTest test = testService.submitTypedText(serviceProviderId,testId, submitTextDto.getTypedText());
            return responseService.generateResponse(HttpStatus.OK,"Text is submitted",test);
        }
        catch (EntityDoesNotExistsException e)
        {
            return ResponseService.generateErrorResponse("Service Provider does not exist",HttpStatus.NOT_FOUND);
        }
        catch (EntityNotFoundException e)
        {
            return ResponseService.generateErrorResponse("Test does not exist",HttpStatus.NOT_FOUND);
        }
        catch (IllegalArgumentException e)
        {
            return ResponseService.generateErrorResponse(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        catch (Exception e)
        {
            return ResponseService.generateErrorResponse("Something went wrong",HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{serviceProviderId}/{testId}/upload-resized-signature")
    public ResponseEntity<?> uploadResizedSignature(@PathVariable Long serviceProviderId,@PathVariable Long testId, @RequestParam("resizedSignature") MultipartFile resizedSignature) throws Exception {
        try
        {
            ServiceProviderTest test = testService.uploadSignatureImage(serviceProviderId,testId, resizedSignature);
            return responseService.generateResponse(HttpStatus.OK,"Signature image is uploaded",test);
        }
         catch (EntityDoesNotExistsException e)
        {
            return ResponseService.generateErrorResponse("Service Provider does not exist",HttpStatus.NOT_FOUND);
        }
        catch (EntityNotFoundException e)
        {
            return ResponseService.generateErrorResponse("Test does not exist",HttpStatus.NOT_FOUND);
        }
        catch (IllegalArgumentException e)
        {
            return ResponseService.generateErrorResponse(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        catch (Exception e)
        {
            return ResponseService.generateErrorResponse("Something went wrong",HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{serviceProviderId}/getAll")
    public ResponseEntity<?> getAllTests(@PathVariable Long serviceProviderId) throws EntityNotFoundException, EntityDoesNotExistsException, CustomerDoesNotExistsException {
        List<ServiceProviderTest> serviceProviderTests= testService.getServiceProviderTestByServiceProviderId(serviceProviderId);
        if(serviceProviderTests.isEmpty())
        {
            return responseService.generateSuccessResponse("Service provider's test list is empty",serviceProviderTests, HttpStatus.OK);
        }
        return responseService.generateSuccessResponse("All tests are found",serviceProviderTests, HttpStatus.OK);
    }
}
