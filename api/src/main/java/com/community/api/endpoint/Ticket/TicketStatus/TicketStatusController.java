package com.community.api.endpoint.Ticket.TicketStatus;

import com.community.api.component.Constant;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.entity.CustomTicketType;
import com.community.api.services.ResponseService;
import com.community.api.services.TicketStatusService;
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
public class TicketStatusController {

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    @Autowired
    TicketStatusService ticketStatusService;

    @GetMapping("/get-all-ticket-status")
    public ResponseEntity<?> getAllTicketStatus() {
        try {
            List<CustomTicketStatus> customTicketStatusList = ticketStatusService.getAllTicketStatus();
            if (customTicketStatusList.isEmpty()) {
                return ResponseService.generateErrorResponse("NO TICKET STATUS IS FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("TICKET STATUSES FOUND", customTicketStatusList, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-ticket-status-by-ticket-status-id/{ticketStatusId}")
    public ResponseEntity<?> getTicketStatusByTicketId(@PathVariable Long ticketStatusId) {
        try {
            CustomTicketStatus ticketStatus = ticketStatusService.getTicketStatusByTicketStatusId(ticketStatusId);
            if (ticketStatus == null) {
                return ResponseService.generateErrorResponse("NO TICKET STATUS FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("TICKET STATUS FOUND", ticketStatus, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
