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
@Table(name = "custom_service_provider_ticket")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomServiceProviderTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long ticketId;

   /* @Column(name = "ticket_type")

    @Column(name = "ticket_status")

    @Column(name = "created_by")

    @Column(name = "creted_date")

    @Column(name = "modified_date")

    @Column(name = "modified_by")

    @Column(name = "ticket_assign_to")

    @Column(name = "target_completion_time")

    @Column(name = "ticket_assign_time")*/

}
