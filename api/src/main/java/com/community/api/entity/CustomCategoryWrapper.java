package com.community.api.entity;

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
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.common.exception.ServiceException;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.common.rest.api.wrapper.APIWrapper;
import org.broadleafcommerce.common.rest.api.wrapper.BaseWrapper;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.search.domain.SearchCriteria;
import org.broadleafcommerce.core.search.domain.SearchResult;
import org.broadleafcommerce.core.search.service.SearchService;

@Data
@NoArgsConstructor
public class CustomCategoryWrapper extends BaseWrapper implements APIWrapper<Category> {

    protected Long id;

    protected String name;

    protected String description;

    protected String longDescription;

    protected Boolean active;

    protected String url;

    protected String urlKey;

    protected Date activeStartDate;

    protected Date activeEndDate;

    protected List<ProductWrapper> products;

    protected Character archived;

    protected String displayTemplate;

    public void wrapDetails(Category category, HttpServletRequest request) {
        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
        this.displayTemplate = category.getDisplayTemplate();
        this.activeStartDate = category.getActiveStartDate();
        this.activeEndDate = category.getActiveEndDate();
        this.url = category.getUrl();
        this.urlKey = category.getUrlKey();

        Integer productLimit = (Integer)request.getAttribute("productLimit");
        Integer productOffset = (Integer)request.getAttribute("productOffset");
        Integer subcategoryLimit = (Integer)request.getAttribute("subcategoryLimit");
        Integer subcategoryOffset = (Integer)request.getAttribute("subcategoryOffset");
        if (productLimit != null && productOffset == null) {
            productOffset = 1;
        }

        if (productLimit != null && productOffset != null) {
            SearchService searchService = this.getSearchService();
            SearchCriteria searchCriteria = new SearchCriteria();
            searchCriteria.setPage(productOffset);
            searchCriteria.setPageSize(productLimit);
            searchCriteria.setFilterCriteria(new HashMap());

            try {
                SearchResult result = searchService.findExplicitSearchResultsByCategory(category, searchCriteria);
                List<Product> productList = result.getProducts();
                if (productList != null && !productList.isEmpty()) {
                    if (this.products == null) {
                        this.products = new ArrayList();
                    }

                    Iterator var11 = productList.iterator();

                    while(var11.hasNext()) {
                        Product p = (Product)var11.next();
                        ProductWrapper productSummaryWrapper = (ProductWrapper)this.context.getBean(ProductWrapper.class.getName());
                        productSummaryWrapper.wrapSummary(p, request);
                        this.products.add(productSummaryWrapper);
                    }
                }
            } catch (ServiceException var14) {
                ServiceException e = var14;
                throw BroadleafWebServicesException.build(500, (Locale)null, (Map)null, e);
            }
        }

        if (category instanceof Status) {
            this.archived = ((Status)category).getArchived();
        }

    }

    public void wrapSummary(Category category, HttpServletRequest request) {
        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
        this.longDescription = category.getLongDescription();
        this.active = category.isActive();

    }

    protected SearchService getSearchService() {
        return (SearchService)this.context.getBean("blSearchService");
    }
}