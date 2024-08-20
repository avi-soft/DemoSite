package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.ProductImpl;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "custom_product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomProduct extends ProductImpl {

    @Column(name = "go_live_date")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date goLiveDate;

    @Column(name = "priority_level")
    @Min(value = 1, message = "Value must be between 1 and 5")
    @Max(value = 5, message = "Value must be between 1 and 5")
    protected int priorityLevel;

    @Column(name = "job_group")
    protected Character jobGroup;

    @Column(name = "platform_fee")
    protected Double platformFee;

    @Column(name = "exam_date_from")
    protected Date examDateFrom;

    @Column(name = "exam_date_to")
    protected Date examDateTo;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "product_state_id")
    protected CustomProductState productState;


}