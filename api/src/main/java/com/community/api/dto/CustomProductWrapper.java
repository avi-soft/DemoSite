package com.community.api.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import com.broadleafcommerce.rest.api.wrapper.MediaWrapper;
import com.community.api.entity.*;
import com.community.api.services.ReserveCategoryService;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.common.rest.api.wrapper.APIWrapper;
import org.broadleafcommerce.common.rest.api.wrapper.BaseWrapper;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.common.persistence.Status;
import org.springframework.beans.factory.annotation.Autowired;


@Data
@NoArgsConstructor
public class CustomProductWrapper extends BaseWrapper implements APIWrapper<Product> {

    protected Long id;
    protected String metaTitle;
    protected String displayTemplate;
    protected String metaDescription;
    protected String longDescription;
    protected String categoryName;
    protected Integer priorityLevel;
    protected Date activeStartDate;
    protected Date activeEndDate;
    protected Date activeGoLiveDate;
    protected Long defaultCategoryId;
    protected Character archived;

    protected String url;
    protected Boolean active;
    protected String promoMessage;
    protected Integer quantity;
    protected List<MediaWrapper> media;

    protected List<ReserveCategoryDto> reserveCategoryDtoList = new ArrayList<>();
    protected Double platformFee;
    protected String notifyingAuthority;
    protected CustomApplicationScope customApplicationScope;
    protected CustomProductState customProductState;
    protected CustomJobGroup customJobGroup;

    protected Long creatorUserId;
    protected Role creatorRoleId;

    protected Date modifiedDate;
    protected String advertiserUrl;
    protected Boolean domicileRequired;
    protected Long modifierUserId;
    protected Role modifierRoleId;

    public void wrapDetailsAddProduct(Product product, AddProductDto addProductDto, CustomJobGroup customJobGroup, CustomProductState customProductState, CustomApplicationScope customApplicationScope, CustomReserveCategory customReserveCategory, Long creatorUserId, Role creatorRole) {

        this.id = product.getId();
        this.metaTitle = product.getMetaTitle();
        this.displayTemplate = product.getDisplayTemplate();
        this.longDescription = product.getLongDescription();
        this.active = product.isActive();
        this.quantity = product.getDefaultSku().getQuantityAvailable();
        this.activeGoLiveDate = addProductDto.getGoLiveDate();
        this.categoryName = product.getDefaultCategory().getName();
        this.priorityLevel = addProductDto.getPriorityLevel();
        this.archived = 'N';
        this.promoMessage = product.getPromoMessage();
        this.activeGoLiveDate = addProductDto.getGoLiveDate();
        this.activeEndDate = product.getDefaultSku().getActiveEndDate();
        this.activeStartDate = product.getDefaultSku().getActiveStartDate();
        this.url = product.getUrl();
        this.metaDescription = product.getMetaDescription();

        this.displayTemplate = product.getDisplayTemplate();

        ReserveCategoryDto reserveCategoryDto = new ReserveCategoryDto();
        reserveCategoryDto.setProductId(product.getId());
        reserveCategoryDto.setReserveCategoryId(addProductDto.getReservedCategory());
        reserveCategoryDto.setReserveCategory(customReserveCategory.getReserveCategoryName());
        reserveCategoryDto.setFee(addProductDto.getFee());
        reserveCategoryDto.setPost(addProductDto.getPost());
        reserveCategoryDto.setBornBefore(addProductDto.getBornBefore());
        reserveCategoryDto.setBornAfter(addProductDto.getBornAfter());

        this.platformFee = addProductDto.getPlatformFee();
        this.notifyingAuthority = addProductDto.notifyingAuthority;

        this.customApplicationScope = customApplicationScope;
        this.customJobGroup = customJobGroup;
        this.customProductState = customProductState;
        this.reserveCategoryDtoList.add(reserveCategoryDto);

        this.modifiedDate = product.getActiveStartDate();
        this.creatorUserId = creatorUserId;
        this.creatorRoleId = creatorRole;
        this.modifierUserId = null;
        this.modifierRoleId = null;

        this.domicileRequired = addProductDto.getDomicileRequired();
        this.advertiserUrl = addProductDto.getAdvertiserUrl();

        if (product.getDefaultCategory() != null) {
            this.defaultCategoryId = product.getDefaultCategory().getId();
        }
    }

    public void wrapDetails(CustomProduct customProduct, List<ReserveCategoryDto> reserveCategoryDtoList) {
        this.id = customProduct.getId();
        this.metaTitle = customProduct.getMetaTitle();
        this.displayTemplate = customProduct.getDisplayTemplate();
        this.longDescription = customProduct.getLongDescription();
        this.active = customProduct.isActive();
        this.quantity = customProduct.getDefaultSku().getQuantityAvailable();
        this.activeGoLiveDate = customProduct.getGoLiveDate();
        this.categoryName = customProduct.getDefaultCategory().getName();
        this.priorityLevel = customProduct.getPriorityLevel();
        this.archived = customProduct.getArchived();
        this.promoMessage = customProduct.getPromoMessage();
        this.activeGoLiveDate = customProduct.getGoLiveDate();
        this.activeEndDate = customProduct.getDefaultSku().getActiveEndDate();
        this.activeStartDate = customProduct.getDefaultSku().getActiveStartDate();
        this.url = customProduct.getUrl();
        this.metaDescription = customProduct.getMetaDescription();

        this.displayTemplate = customProduct.getDisplayTemplate();
        this.platformFee = customProduct.getPlatformFee();
        this.notifyingAuthority = customProduct.getNotifyingAuthority();

        this.customApplicationScope = customProduct.getCustomApplicationScope();
        this.customJobGroup = customProduct.getJobGroup();
        this.customProductState = customProduct.getProductState();
        this.reserveCategoryDtoList = reserveCategoryDtoList;

        this.modifiedDate = customProduct.getModifiedDate();

        this.creatorUserId = customProduct.getUserId();
        this.creatorRoleId = customProduct.getCreatoRole();
        this.modifierUserId = customProduct.getModifierUserId();
        this.modifierRoleId = customProduct.getModifierRole();

        this.domicileRequired = customProduct.getDomicileRequired();
        this.advertiserUrl = customProduct.getAdvertiserUrl();

        if (customProduct.getDefaultCategory() != null) {
            this.defaultCategoryId = customProduct.getDefaultCategory().getId();
        }
    }
    public void wrapDetails(CustomProduct customProduct) {
        this.id = customProduct.getId();
        this.metaTitle = customProduct.getMetaTitle();
        this.displayTemplate = customProduct.getDisplayTemplate();
        this.longDescription = customProduct.getLongDescription();
        this.active = customProduct.isActive();
        this.quantity = customProduct.getDefaultSku().getQuantityAvailable();
        this.activeGoLiveDate = customProduct.getGoLiveDate();
        this.categoryName = customProduct.getDefaultCategory().getName();
        this.priorityLevel = customProduct.getPriorityLevel();
        this.archived = customProduct.getArchived();
        this.promoMessage = customProduct.getPromoMessage();
        this.activeGoLiveDate = customProduct.getGoLiveDate();
        this.activeEndDate = customProduct.getDefaultSku().getActiveEndDate();
        this.activeStartDate = customProduct.getDefaultSku().getActiveStartDate();
        this.url = customProduct.getUrl();
        this.metaDescription = customProduct.getMetaDescription();

        this.displayTemplate = customProduct.getDisplayTemplate();
        this.platformFee = customProduct.getPlatformFee();
        this.notifyingAuthority = customProduct.getNotifyingAuthority();

        this.customApplicationScope = customProduct.getCustomApplicationScope();
        this.customJobGroup = customProduct.getJobGroup();
        this.customProductState = customProduct.getProductState();

        this.creatorUserId = customProduct.getUserId();
        this.creatorRoleId = customProduct.getCreatoRole();
        this.modifierUserId = customProduct.getModifierUserId();
        this.modifierRoleId = customProduct.getModifierRole();

        this.domicileRequired = customProduct.getDomicileRequired();
        this.advertiserUrl = customProduct.getAdvertiserUrl();

        if (customProduct.getDefaultCategory() != null) {
            this.defaultCategoryId = customProduct.getDefaultCategory().getId();
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
        this.quantity = product.getDefaultSku().getQuantityAvailable();

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