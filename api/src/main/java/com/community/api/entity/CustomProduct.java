package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.ProductImpl;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "custom_product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomProduct extends ProductImpl {

    @Column(name = "go_live_date")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date goLiveDate;

    @Column(name = "priority_level")
    @Min(value = 1, message = "Value must be between 1 and 5")
    @Max(value = 5, message = "Value must be between 1 and 5")
    protected Integer priorityLevel;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "job_group_id")
    protected CustomJobGroup jobGroup;

    @Column(name = "platform_fee")
    protected Double platformFee;

    @Column(name = "exam_date_from")
    protected Date examDateFrom;

    @Column(name = "exam_date_to")
    protected Date examDateTo;

    @Column(name = "last_modified")
    protected Date modifiedDate;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "product_state_id")
    protected CustomProductState productState;

    @ManyToOne
    @JoinColumn(name = "application_scope_id")
    protected CustomApplicationScope customApplicationScope;

    @ManyToOne
    @JoinColumn(name = "creator_role_id")
    protected Role creatoRole;

    @Column(name = "creator_user_id")
    protected Long userId;

    @ManyToOne
    @JoinColumn(name = "notifying_authority")
    protected StateCode notifyingAuthority;

    @Column(name = "advertiser_url")
    String advertiserUrl;

    @Column(name = "domicile_required")
    Boolean domicileRequired;

    @Column(name = "modifier_user_id")
    Long modifierUserId;

    @ManyToOne
    @JoinColumn(name = "modifier_role_id")
    Role modifierRole;

    @ManyToOne
    @JoinColumn(name = "rejection_status_id")
    CustomProductRejectionStatus rejectionStatus;

    @Column(name = "last_date_to_pay_fee")
    Date lateDateToPayFee;

    @Column(name = "admit_card_date_from")
    Date admitCardDateFrom;

    @Column(name = "admit_card_date_to")
    Date adminCardDateTo;

    @Column(name = "modification_date_from")
    Date modificationDateFrom;

    @Column(name = "modification_date_to")
    Date modificationDateTo;

    @Column(name = "download_notification_link")
    String downloadNotificationLink;

    @Column(name = "download_syllabus_link")
    String downloadSyllabusLink;

    @Column(name = "form_complexity")
    @Min(value = 1, message = "Value must be between 1 and 5")
    @Max(value = 5, message = "Value must be between 1 and 5")
    Long formComplexity;

    @ManyToOne
    @JoinColumn(name = "qualification")
    Qualification qualification;
    @ManyToOne
    @JoinColumn(name ="stream")
    CustomStream stream;
    @ManyToOne
    @JoinColumn(name = "subject")
    CustomSubject subject;
    @ManyToOne
    @JoinColumn(name = "gender_specific")
    CustomGender gender_specific;
    @Column(name = "selection_criteria")
    String selectionCriteria;
    @ManyToOne
    @JoinColumn(name = "sector")
    CustomSector sector;

}