package com.community.api.endpoint.avisoft.controller.Stream;

import com.community.api.component.Constant;
import com.community.api.dto.AddProductDto;
import com.community.api.dto.AddStreamDto;
import com.community.api.entity.CustomStream;
import com.community.api.services.ResponseService;
import com.community.api.services.StreamService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StreamController {

    private final ExceptionHandlingService exceptionHandlingService;
    private final StreamService streamService;

    @Autowired
    public StreamController(ExceptionHandlingService exceptionHandlingService, StreamService streamService) {
        this.exceptionHandlingService = exceptionHandlingService;
        this.streamService = streamService;
    }

    @PostMapping("/add-stream")
    public ResponseEntity<?> addStream(@RequestBody AddStreamDto addStreamDto, @RequestHeader(value = "Authorization") String authHeader) {
        try{
            if(!streamService.validiateAuthorization(authHeader)) {
                return ResponseService.generateErrorResponse("NOT AUTHORIZED TO ADD A STREAM", HttpStatus.UNAUTHORIZED);
            }

            streamService.validateAddStreamDto(addStreamDto);
            streamService.saveStream(addStreamDto);

            return ResponseService.generateSuccessResponse("SUCCESSFULLY ADDED", addStreamDto, HttpStatus.OK);
        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + numberFormatException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-all-stream")
    public ResponseEntity<?> getAllStream() {
        try {
            List<CustomStream> applicationScopeList = streamService.getAllStream();
            if (applicationScopeList.isEmpty()) {
                return ResponseService.generateErrorResponse("NO STREAM IS FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("STREAMS FOUND", applicationScopeList, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-stream-by-id/{streamId}")
    public ResponseEntity<?> getStreamByStreamId(@PathVariable Long streamId) {
        try {
            CustomStream stream = streamService.getStreamByStreamId(streamId);
            if (stream == null) {
                return ResponseService.generateErrorResponse("NO STREAM FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("STREAM FOUND", stream, HttpStatus.OK);
        }catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
