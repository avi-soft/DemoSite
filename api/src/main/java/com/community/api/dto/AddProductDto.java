package com.community.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.common.money.Money;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddProductDto {

    Money cost;
    String metaTitle;
    String metaDescription;
    Date activeStartDate;
    Date activeEndDate;
    Date goLiveDate;
    Integer priorityLevel;
    String url;
    Integer quantity;

}
