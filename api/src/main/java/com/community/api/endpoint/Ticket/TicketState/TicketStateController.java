package com.community.api.endpoint.Ticket.TicketState;

import com.community.api.component.Constant;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.services.ResponseService;
import com.community.api.services.TicketStateService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TicketStateController {

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    @Autowired
    TicketStateService ticketStateService;

    @GetMapping("/get-all-ticket-states")
    public ResponseEntity<?> getAllTicketStates() {
        try {
            List<CustomTicketState> customTicketStateList = ticketStateService.getAllTicketState();
            if (customTicketStateList.isEmpty()) {
                return ResponseService.generateErrorResponse("NO TICKET STATE IS FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("TICKET STATES FOUND", customTicketStateList, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-ticket-state-by-ticket-state-id/{ticketStateId}")
    public ResponseEntity<?> getTicketStateByTicketStateId(@PathVariable Long ticketStateId) {
        try {
            CustomTicketState ticketState = ticketStateService.getTicketStateByTicketId(ticketStateId);
            if (ticketState == null) {
                return ResponseService.generateErrorResponse("NO TICKET STATE FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("TICKET STATE FOUND", ticketState, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
