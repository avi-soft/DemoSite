package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddProductDto {

    @JsonProperty("meta_title")
    @NotNull
    String metaTitle;

    @JsonProperty("fee")
    @NotNull
    Double fee;

    @JsonProperty("platform_fee")
    @NotNull
    Double platformFee;

    @JsonProperty("application_scope")
    @NotNull
    Long applicationScope;

    @JsonProperty("job_group")
    Long jobGroup;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("active_end_date")
    Date activeEndDate;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("go_live_date")
    Date goLiveDate;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("exam_date_from")
    Date examDateFrom;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("exam_date_to")
    Date examDateTo;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("born_after")
    Date bornAfter;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("born_before")
    Date bornBefore;

    @JsonProperty("priority_level")
    Integer priorityLevel;
    @JsonProperty("meta_description")
    String metaDescription;

    @JsonProperty("reserve_category")
    List<AddReserveCategoryDto> reservedCategory;

    @JsonProperty("notifying_authority")
    String notifyingAuthority;
    @JsonProperty("post")
    Integer post;
    @JsonProperty("quantity")
    Integer quantity;
    @JsonProperty("advertiser_url")
    String advertiserUrl;
    @JsonProperty("domicile_required")
    Boolean domicileRequired;
    @JsonProperty("product_state")
    Long productState;
    @JsonProperty("display_template")
    String displayTemplate;

}