package com.community.api.dto;

import com.community.api.entity.CustomProduct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.core.catalog.domain.ProductImpl;
import org.broadleafcommerce.core.catalog.domain.SkuImpl;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddProductDto {

    Money cost;
    Long id;
    String metaTitle;
    String metaDescription;
    Date activeStartDate;
    Date activeEndDate;
    Date goLiveDate;
    Integer priorityLevel;
    String url;
    Integer quantity;
    Integer post;

}
