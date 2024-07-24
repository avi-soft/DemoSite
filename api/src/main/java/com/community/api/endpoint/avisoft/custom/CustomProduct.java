package com.community.api.endpoint.avisoft.custom;

import org.broadleafcommerce.core.catalog.domain.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "ext_product")
public class CustomProduct extends ProductImpl {

    @Temporal(TemporalType.TIMESTAMP)
    protected Date goLiveDate;

    public CustomProduct() {
    }

    public CustomProduct(Date goLiveDate) {
        this.goLiveDate = goLiveDate;
    }

    public Date getGoLiveDate() {
        return goLiveDate;
    }

    public void setGoLiveDate(Date goLiveDate) {
        this.goLiveDate = goLiveDate;
    }

}