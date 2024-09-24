package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "custom_ticket_type")
public class CustomTicketType {

    @Id
    @Column(name = "ticket_type_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long ticketTypeId;

    @Column(name = "ticket_type")
    protected String ticketType;

    @Column(name = "ticket_type_description")
    protected String ticketTypeDescription;

}
