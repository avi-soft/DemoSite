package com.community.api.dto;

import java.util.Date;
import javax.servlet.http.HttpServletRequest;

import com.community.api.entity.CustomProduct;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.common.rest.api.wrapper.APIWrapper;
import org.broadleafcommerce.common.rest.api.wrapper.BaseWrapper;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.common.persistence.Status;

@Data
@NoArgsConstructor
public class CustomProductWrapper extends BaseWrapper implements APIWrapper<Product> {

    protected Long id;

    protected String metaTitle;

    protected String metaDescription;

    protected String longDescription;

    protected Double cost;

    protected String categoryName;

    protected Integer priorityLevel;

    protected String url;

    protected Boolean active;

    protected Date activeStartDate;
    protected Date activeEndDate;
    protected Date activeGoLiveDate;

    protected String promoMessage;

    protected Long defaultCategoryId;

    protected Character archived;

    public void wrapDetails(CustomProduct model) {
        this.id = model.getId();
        this.metaTitle = model.getMetaTitle();
        this.metaDescription = model.getMetaDescription();
        this.longDescription = model.getLongDescription();
        this.url = model.getUrl();
        this.cost = model.getDefaultSku().getCost().doubleValue();
        this.activeStartDate = model.getDefaultSku().getActiveStartDate();
        this.activeEndDate = model.getDefaultSku().getActiveEndDate();
        this.activeGoLiveDate = model.getGoLiveDate();
        this.promoMessage = model.getPromoMessage();
        this.archived = model.getArchived();
        this.priorityLevel = model.getPriorityLevel();
        this.categoryName = model.getDefaultCategory().getName();
        this.active = model.isActive();
        this.activeGoLiveDate = model.getGoLiveDate();

        if (model.getDefaultCategory() != null) {
            this.defaultCategoryId = model.getDefaultCategory().getId();
        }
    }

    @Override
    public void wrapDetails(Product product, HttpServletRequest httpServletRequest) {
        this.id = product.getId();
        this.metaTitle = product.getMetaTitle();
        this.metaDescription = product.getMetaDescription();
        this.longDescription = product.getLongDescription();
        this.url = product.getUrl();
        this.activeStartDate = product.getDefaultSku().getActiveStartDate();
        this.activeEndDate = product.getDefaultSku().getActiveEndDate();
        this.promoMessage = product.getPromoMessage();
        this.archived = ((Status) product).getArchived();
        this.categoryName = product.getDefaultCategory().getName();
        this.active = product.isActive();
        this.cost = product.getDefaultSku().getCost().doubleValue();

        if (product.getDefaultCategory() != null) {
            this.defaultCategoryId = product.getDefaultCategory().getId();
        }
    }

    public void wrapDetails(Product product, Integer priorityLevel, Date activeGoLiveDate) {
        this.id = product.getId();
        this.metaTitle = product.getMetaTitle();
        this.metaDescription = product.getMetaDescription();
        this.longDescription = product.getLongDescription();
        this.url = product.getUrl();
        this.activeStartDate = product.getDefaultSku().getActiveStartDate();
        this.activeEndDate = product.getDefaultSku().getActiveEndDate();
        this.promoMessage = product.getPromoMessage();
        this.archived = ((Status) product).getArchived();
        this.categoryName = product.getDefaultCategory().getName();
        this.active = product.isActive();
        this.activeGoLiveDate = activeGoLiveDate;
        this.priorityLevel = priorityLevel;
        this.cost = product.getDefaultSku().getCost().doubleValue();

        if (product.getDefaultCategory() != null) {
            this.defaultCategoryId = product.getDefaultCategory().getId();
        }

    }

    public void wrapSummary(Product model, HttpServletRequest request) {
        this.id = model.getId();
        this.metaTitle = model.getName();
        this.metaDescription = model.getDescription();
        this.longDescription = model.getLongDescription();
        this.url = model.getUrl();
        this.active = model.isActive();
    }
}