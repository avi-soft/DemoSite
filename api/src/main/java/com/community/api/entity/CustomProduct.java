package com.community.api.entity;

import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.ProductImpl;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "custom_product")
public class CustomProduct extends ProductImpl {

    @Column(name = "go_live_date")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date goLiveDate;

    @Column(name = "priority_level")
    @Min(value = 1, message = "Value must be between 1 and 5")
    @Max(value = 5, message = "Value must be between 1 and 5")
    protected int priorityLevel;

    public CustomProduct() {
    }

    public CustomProduct(Date goLiveDate, Integer priorityLevel) {
        this.goLiveDate = goLiveDate;
        this.priorityLevel = priorityLevel;
    }

    public Date getGoLiveDate() {
        return goLiveDate;
    }

    public void setGoLiveDate(Date goLiveDate) {
        this.goLiveDate = goLiveDate;
    }

    public int getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(int priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

}