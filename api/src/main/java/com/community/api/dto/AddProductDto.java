package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.common.money.Money;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddProductDto {

    @NotNull
    String metaTitle;
    @NotNull
    Double fee;
    @NotNull
    Double platformFee;
    @NotNull
    Long applicationScope;

    Long jobGroup;

    @Temporal(TemporalType.TIMESTAMP)
    Date activeEndDate;
    @Temporal(TemporalType.TIMESTAMP)
    Date goLiveDate;
    @Temporal(TemporalType.TIMESTAMP)
    Date examDateFrom;
    @Temporal(TemporalType.TIMESTAMP)
    Date examDateTo;
    @Temporal(TemporalType.TIMESTAMP)
    Date bornAfter;
    @Temporal(TemporalType.TIMESTAMP)
    Date bornBefore;

    Integer priorityLevel;
    String metaDescription;
    Long reservedCategory;
    String notifyingAuthority;
    Integer post;
    Integer quantity;

    String advertiserUrl;
    Boolean domicileRequired;

}
