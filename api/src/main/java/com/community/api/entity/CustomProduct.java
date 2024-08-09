package com.community.api.entity;

import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.ProductImpl;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "ext_product")
public class CustomProduct extends ProductImpl {

    @Temporal(TemporalType.TIMESTAMP)
    protected Date goLiveDate;

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