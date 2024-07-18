package com.community.api.endpoint.avisoft.custom;

import org.broadleafcommerce.core.catalog.domain.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "ext_product")
public class CustomProduct extends ProductImpl {

    private Date created_date;
    protected Date expiration_date;
    protected Date go_live_date;

    public CustomProduct() {
    }

    public CustomProduct(Product original, Date created_date, Date expiration_date, Date go_live_date) {
        this.created_date = created_date;
        this.expiration_date = expiration_date;
        this.go_live_date = go_live_date;
    }

    public Date getCreated_date() {
        return created_date;
    }

    public Date getExpiration_date() {
        return expiration_date;
    }

    public Date getGo_live_date() {
        return go_live_date;
    }

    public void setCreated_date(Date created_date) {
        this.created_date = created_date;
    }

    public void setExpiration_date(Date expiration_date) {
        this.expiration_date = expiration_date;
    }

    public void setGo_live_date(Date go_live_date) {
        this.go_live_date = go_live_date;
    }

}
