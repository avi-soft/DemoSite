package com.community.api.entity;

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
public class CustomProduct extends ProductImpl {

    @Column(name = "go_live_date")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date goLiveDate;

    @Column(name = "priority_level")
    @Min(value = 1, message = "Value must be between 1 and 5")
    @Max(value = 5, message = "Value must be between 1 and 5")
    protected int priorityLevel;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "product_state_id")
    protected CustomProductState productState;

    public CustomProduct(Date goLiveDate, Integer priorityLevel, CustomProductState customProductState) {
        this.goLiveDate = goLiveDate;
        this.priorityLevel = priorityLevel;
        this.productState = customProductState;
    }


}