package com.community.api.endpoint.Ticket;

import com.community.api.component.Constant;
import com.community.api.dto.CreateTicketDto;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.services.ResponseService;
import com.community.api.services.TicketService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@RestController("/ticket-custom")
@RequestMapping(value = "/product-custom", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
public class TicketController {

    @Autowired
    TicketService ticketService;
    @Autowired
    ExceptionHandlingService exceptionHandlingService;
    @PersistenceContext
    EntityManager entityManager;

    /*@Transactional
    @PostMapping("/add")
    public ResponseEntity<?> createTicket(@RequestBody CreateTicketDto createTicketDto) {

        try{
            CustomServiceProviderTicket customServiceProviderTicket = entityMangaer.
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }*/
}
