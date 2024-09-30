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
@Table(name = "custom_product_rejection_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomProductRejectionStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rejection_status_id")
    protected Long rejectionStatusId;

    @Column(name = "rejection_status")
    protected String rejectionStatus;

    @Column(name = "rejection_status_description")
    protected String rejectionStatusDescription;

}
