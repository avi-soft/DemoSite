package com.community.api.endpoint.Ticket;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;

@RestController("/ticket-custom")
@RequestMapping(value = "/product-custom", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
public class TicketController {

    @Transactional
    @PostMapping("/add")
    public ResponseEntity<?> createTicket() {

    }
}
