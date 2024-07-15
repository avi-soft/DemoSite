package com.community.api.endpoint.avisoft.custom;

import org.broadleafcommerce.core.catalog.domain.*;

import javax.persistence.Entity;
import java.util.Date;

@Entity
@javax.persistence.Table(name = "BLC_PRODUCT")
public class CustomProduct extends ProductImpl {

    protected Date created_date;
    protected Date expiration_date;
    protected Date go_live_date;

/*    public CustomProduct(Date created_date, Date expiration_date, Date go_live_date) {
        this.created_date = created_date;
        this.expiration_date = expiration_date;
        this.go_live_date = go_live_date;
    }*/

    @Override
    public String toString() {
        return "CustomProduct{" +
                "created_date=" + created_date +
                ", expiration_date=" + expiration_date +
                ", go_live_date=" + go_live_date +
                ", id=" + id +
                ", url='" + url + '\'' +
                ", overrideGeneratedUrl=" + overrideGeneratedUrl +
                ", urlKey='" + urlKey + '\'' +
                ", displayTemplate='" + displayTemplate + '\'' +
                ", model='" + model + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", isFeaturedProduct=" + isFeaturedProduct +
                ", defaultSku=" + defaultSku +
                ", canSellWithoutOptions=" + canSellWithoutOptions +
                ", metaTitle='" + metaTitle + '\'' +
                ", metaDescription='" + metaDescription + '\'' +
                ", canonicalUrl='" + canonicalUrl + '\'' +
                ", skus=" + skus +
                ", promoMessage='" + promoMessage + '\'' +
                ", crossSaleProducts=" + crossSaleProducts +
                ", upSaleProducts=" + upSaleProducts +
                ", additionalSkus=" + additionalSkus +
                ", defaultCategory=" + defaultCategory +
                ", allParentCategoryXrefs=" + allParentCategoryXrefs +
                ", productAttributes=" + productAttributes +
                ", productOptions=" + productOptions +
                ", productOptionMap=" + productOptionMap +
                ", allParentCategoryIds=" + allParentCategoryIds +
                ", archiveStatus=" + archiveStatus +
                '}';
    }

    public CustomProduct(Product original, Date created_date, Date expiration_date, Date go_live_date) {
        this.id = original.getId();
        this.manufacturer = original.getManufacturer();
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
