package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "custom_ticket_status")
public class CustomTicketStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_status_id")
    protected Long ticketStatusId;

    @Column(name = "ticket_status")
    protected String ticketStatus;

    @Column(name = "ticket_status_description")
    protected String ticketStatusDescription;

}