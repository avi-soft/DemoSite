package com.community.api.dto;

import com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import com.broadleafcommerce.rest.api.wrapper.ProductWrapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.common.exception.ServiceException;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.common.rest.api.wrapper.APIWrapper;
import org.broadleafcommerce.common.rest.api.wrapper.BaseWrapper;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.search.domain.SearchCriteria;
import org.broadleafcommerce.core.search.domain.SearchResult;
import org.broadleafcommerce.core.search.service.SearchService;

@Data
@NoArgsConstructor
public class CustomCategoryWrapper extends BaseWrapper implements APIWrapper<Category> {

    @JsonProperty("category_id")
    protected Long id;
    @JsonProperty("name")
    protected String name;
    @JsonProperty("description")
    protected String description;
    @JsonProperty("long_description")
    protected String longDescription;
    @JsonProperty("active")
    protected Boolean active;
    @JsonProperty("url")
    protected String url;
    @JsonProperty("url_key")
    protected String urlKey;
    @JsonProperty("active_start_date")
    protected Date activeStartDate;

    @JsonProperty("active_end_date")
    protected Date activeEndDate;
    @JsonProperty("archived")
    protected Character archived;
    @JsonProperty("display_template")
    protected String displayTemplate;
    @JsonProperty("total_products")
    Integer totalProducts;

    @JsonProperty("products")
    List<CustomProductWrapper> products;

    public void wrapDetailsCategory(Category category, List<CustomProductWrapper> products, HttpServletRequest request) {

        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
        this.longDescription = category.getLongDescription();
        this.active = category.isActive();
        this.displayTemplate = category.getDisplayTemplate();
        this.activeStartDate = category.getActiveStartDate();
        this.activeEndDate = category.getActiveEndDate();
        this.url = category.getUrl();
        this.urlKey = category.getUrlKey();
        this.archived = ((Status) category).getArchived();
        this.products = products;
        if (products == null) {
            this.totalProducts = 0;
        } else {
            this.totalProducts = products.size();
        }

        Integer productLimit = (Integer) request.getAttribute("productLimit");
        Integer productOffset = (Integer) request.getAttribute("productOffset");
        Integer subcategoryLimit = (Integer) request.getAttribute("subcategoryLimit");
        Integer subcategoryOffset = (Integer) request.getAttribute("subcategoryOffset");
        if (productLimit != null && productOffset == null) {
            productOffset = 1;
        }

        if (productLimit != null && productOffset != null) {
            SearchService searchService = this.getSearchService();
            SearchCriteria searchCriteria = new SearchCriteria();
            searchCriteria.setPage(productOffset);
            searchCriteria.setPageSize(productLimit);
            searchCriteria.setFilterCriteria(new HashMap());

        }

        if (category instanceof Status) {
            this.archived = ((Status) category).getArchived();
        }

    }

    @Override
    public void wrapDetails(Category category, HttpServletRequest httpServletRequest) {

    }

    public void wrapSummary(Category category, HttpServletRequest request) {
        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
        this.longDescription = category.getLongDescription();
        this.active = category.isActive();

    }

    protected SearchService getSearchService() {
        return (SearchService) this.context.getBean("blSearchService");
    }
}