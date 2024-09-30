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

    @JsonProperty("platform_fee")
    @NotNull
    Double platformFee;

    @JsonProperty("application_scope_id")
    @NotNull
    Long applicationScope;

    @JsonProperty("job_group_id")
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

    @JsonProperty("priority_level")
    Integer priorityLevel;
    @JsonProperty("meta_description")
    String metaDescription;

    @JsonProperty("reserve_category")
    List<AddReserveCategoryDto> reservedCategory;

    @JsonProperty("physical_requirement")
    List<AddPhysicalRequirementDto> physicalRequirement;

    @JsonProperty("state_id")
    Integer state;
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

    @JsonProperty("rejection_status")
    Long rejectionStatus;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("last_date_to_pay_fee")
    Date lastDateToPayFee;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("admit_card_date_from")
    Date admitCardDateFrom;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("admit_card_date_to")
    Date admitCardDateTo;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("modification_date_from")
    Date modificationDateFrom;
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("modification_date_to")
    Date modificationDateTo;
    @JsonProperty("download_notification_link")
    String downloadNotificationLink;
    @JsonProperty("download_syllabus_link")
    String downloadSyllabusLink;
    @JsonProperty("form_complexity")
    Long formComplexity;

    @JsonProperty("qualification_id")
    Long qualification;
    @JsonProperty("stream_id")
    Long stream;
    @JsonProperty("subject_id")
    Long subject;
    @JsonProperty("gender_specific_id")
    Long genderSpecific;
    @JsonProperty("selection_criteria")
    String selectionCriteria;
    @JsonProperty("sector_id")
    Long sector;

    @JsonProperty("notifying_authority")
    String notifyingAuthority;

}