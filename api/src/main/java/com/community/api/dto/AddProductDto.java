package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.common.money.Money;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
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

    @Temporal(TemporalType.TIMESTAMP)
    Date examDateFrom;
    @Temporal(TemporalType.TIMESTAMP)
    Date examDateTo;
    Long notifyingAuthorityId;
    Money platformFee;
    Character jobGroup;
    Long reservedCategory;

    @Temporal(TemporalType.TIMESTAMP)
    Long bornAfter;
    @Temporal(TemporalType.TIMESTAMP)
    Long bornBefore;

    Integer post;
    Double fee;

    @JsonIgnore
    String url;

    Integer quantity;

}
