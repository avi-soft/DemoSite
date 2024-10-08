package com.community.api.endpoint.Ticket.TicketType;

import com.community.api.component.Constant;
import com.community.api.entity.CustomJobGroup;
import com.community.api.entity.CustomTicketType;
import com.community.api.services.ResponseService;
import com.community.api.services.TicketTypeService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TicketTypeController {

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    @Autowired
    TicketTypeService ticketTypeService;

    @GetMapping("/get-all-ticket-type")
    public ResponseEntity<?> getAllTicketType() {
        try {
            List<CustomTicketType> customTicketTypeList = ticketTypeService.getAllTicketType();
            if (customTicketTypeList.isEmpty()) {
                return ResponseService.generateErrorResponse("NO TICKET TYPE IS FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("TICKET TYPES FOUND", customTicketTypeList, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-ticket-type-by-ticket-type-id/{ticketTypeId}")
    public ResponseEntity<?> getTicketTypeByTicketId(@PathVariable Long ticketTypeId) {
        try {
            CustomTicketType ticketType = ticketTypeService.getTicketTypeByTicketTypeId(ticketTypeId);
            if (ticketType == null) {
                return ResponseService.generateErrorResponse("NO TICKET TYPE FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("TICKET TYPE FOUND", ticketType, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
