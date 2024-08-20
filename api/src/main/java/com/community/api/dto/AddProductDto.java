package com.community.api.dto;

import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomSku;
import lombok.Data;
import org.broadleafcommerce.core.catalog.domain.ProductImpl;
import org.broadleafcommerce.core.catalog.domain.SkuImpl;

@Data
public class AddProductDto {

    protected CustomSku sku;
//    protected ProductImpl productImpl;
//    protected CustomProduct customProduct;

}
