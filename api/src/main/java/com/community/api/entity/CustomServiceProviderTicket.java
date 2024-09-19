package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "custom_service_provider_ticket")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomServiceProviderTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long ticketId;

    @ManyToOne
    @JoinColumn(name = "ticket_state")
    CustomTicketState ticketState;

    @ManyToOne
    @JoinColumn(name = "ticket_status")
    CustomTicketStatus ticketStatus;

    @Column(name = "created_by")
    private Long userId;

    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "modified_date")
    private Date modifiedDate;

    @Column(name = "modified_by")
    private Long modifierId;

    @Column(name = "ticket_assign_to")
    private Long assignTo;

    @Column(name = "target_completion_time")
    private Date targetCompletionDate;

    @Column(name = "ticket_assign_time")
    private Date ticketAssignDate;

}