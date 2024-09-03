package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    Date activeEndDate;
    Date goLiveDate;
    Integer priorityLevel;

    @JsonIgnore
    String url;

    Integer quantity;

}
