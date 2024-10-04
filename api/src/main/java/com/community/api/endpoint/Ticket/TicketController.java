package com.community.api.endpoint.Ticket;

import com.community.api.component.Constant;
import com.community.api.dto.CreateTicketDto;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.entity.CustomTicketType;
import com.community.api.services.ProductService;
import com.community.api.services.ResponseService;
import com.community.api.services.ServiceProviderTicketService;
import com.community.api.services.TicketStateService;
import com.community.api.services.TicketStatusService;
import com.community.api.services.TicketTypeService;
import com.community.api.services.exception.ExceptionHandlingService;
import jsinterop.annotations.JsOverlay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@RestController
@RequestMapping(value = "/ticket-custom", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
public class TicketController {

    @Autowired
    ServiceProviderTicketService serviceProviderTicketService;

    @Autowired
    TicketStateService ticketStateService;

    @Autowired
    TicketStatusService ticketStatusService;

    @Autowired
    TicketTypeService ticketTypeService;

    @Autowired
    ProductService productService;

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    @PersistenceContext
    EntityManager entityManager;

    @Transactional
    @PostMapping("/add")
    public ResponseEntity<?> createTicket(@RequestBody CreateTicketDto createTicketDto, @RequestHeader(value = "Authorization") String authHeader) {

        try {
            CustomServiceProviderTicket customServiceProviderTicket = new CustomServiceProviderTicket();

            if (createTicketDto.getTicketState() == null || createTicketDto.getTicketState() <= 0) {
                ResponseService.generateErrorResponse("TICKET STATE CANNOT BE NULL OR <= 0", HttpStatus.NOT_FOUND);
            }
            CustomTicketState ticketState = ticketStateService.getTicketStateByTicketId(createTicketDto.getTicketState());
            if (ticketState == null) {
                ResponseService.generateErrorResponse("TICKET STATE NOT FOUND WITH THIS ID", HttpStatus.NOT_FOUND);
            }
            customServiceProviderTicket.setTicketState(ticketState);

            if (createTicketDto.getTicketType() != null) {
                if (createTicketDto.getTicketType() <= 0) {
                    ResponseService.generateErrorResponse("TICKET TYPE CANNOT BE <= 0", HttpStatus.NOT_FOUND);
                }
            }
            CustomTicketType ticketType = ticketTypeService.getTicketTypeByTicketTypeId(createTicketDto.getTicketType());
            if (ticketType == null) {
                ResponseService.generateErrorResponse("TICKET STATE NOT FOUND WITH THIS ID", HttpStatus.NOT_FOUND);
            }
            customServiceProviderTicket.setTicketType(ticketType);

            if (createTicketDto.getTicketStatus() != null) {
                if (createTicketDto.getTicketStatus() <= 0) {
                    ResponseService.generateErrorResponse("TICKET STATUS CANNOT BE <= 0", HttpStatus.NOT_FOUND);
                }
                CustomTicketStatus ticketStatus = ticketStatusService.getTicketStatusByTicketStatusId(createTicketDto.getTicketStatus());
                if (ticketStatus == null) {
                    ResponseService.generateErrorResponse("TICKET STATUS NOT FOUND WITH THIS ID", HttpStatus.NOT_FOUND);
                }
                customServiceProviderTicket.setTicketStatus(ticketStatus);
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
            String formattedDate = dateFormat.format(new Date());
            Date createdDate = dateFormat.parse(formattedDate);

            if(createTicketDto.getTargetCompletionDate()!= null) {
                dateFormat.parse(dateFormat.format(createTicketDto.getTargetCompletionDate()));
                if(!createTicketDto.getTargetCompletionDate().after(createdDate)) {
                    ResponseService.generateErrorResponse("TARGET COMPLETION DATE MUST BE OF FUTURE", HttpStatus.NOT_FOUND);
                }
            }else{
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(createdDate);
                calendar.add(Calendar.HOUR_OF_DAY, 4);
                Date newTargetDate = calendar.getTime();

                createTicketDto.setTargetCompletionDate(newTargetDate);
            }

            customServiceProviderTicket.setTargetCompletionDate(createTicketDto.getTargetCompletionDate());
            customServiceProviderTicket.setCreatedDate(createdDate);

            Long creatorUserId = productService.getUserIdByToken(authHeader);
            customServiceProviderTicket.setUserId(creatorUserId);

            customServiceProviderTicket = entityManager.merge(customServiceProviderTicket);
            return ResponseService.generateSuccessResponse("TICKET CREATED SUCCESSFULLY", customServiceProviderTicket,HttpStatus.OK);

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
