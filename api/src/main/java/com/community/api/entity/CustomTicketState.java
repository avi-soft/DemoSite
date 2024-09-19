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
@Table(name = "custom_ticket_type")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomTicketState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_type_id")
    protected Long ticketTypeId;

    @Column(name = "ticket_type")
    protected String ticketType;

    @Column(name = "ticket_description")
    protected String ticketDescription;

}