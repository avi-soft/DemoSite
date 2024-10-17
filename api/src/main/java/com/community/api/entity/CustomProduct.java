package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.core.catalog.domain.ProductImpl;
import org.springframework.lang.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;

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
    @JoinColumn(name = "state_id")
    protected StateCode state;

    @NotNull
    @Column(name = "advertiser_url")
    protected String advertiserUrl;

    @Column(name = "domicile_required")
    protected Boolean domicileRequired;

    @Column(name = "modifier_user_id")
    protected Long modifierUserId;

    @ManyToOne
    @JoinColumn(name = "modifier_role_id")
    protected Role modifierRole;

    @ManyToOne
    @JoinColumn(name = "rejection_status_id")
    protected CustomProductRejectionStatus rejectionStatus;

    @Column(name = "last_date_to_pay_fee")
    protected Date lateDateToPayFee;

    @Nullable
    @Column(name = "admit_card_date_from")
    protected Date admitCardDateFrom;

    @Nullable
    @Column(name = "admit_card_date_to")
    protected Date admitCardDateTo;

    @Column(name = "modification_date_from")
    protected Date modificationDateFrom;

    @Column(name = "modification_date_to")
    protected Date modificationDateTo;

    @Column(name = "download_notification_link")
    protected String downloadNotificationLink;

    @Column(name = "download_syllabus_link")
    protected String downloadSyllabusLink;

    @Column(name = "form_complexity")
    @Min(value = 1, message = "Value must be between 1 and 5")
    @Max(value = 5, message = "Value must be between 1 and 5")
    protected Long formComplexity;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "qualification_id")
    protected Qualification qualification;

    @ManyToOne
    @JoinColumn(name = "stream_id")
    protected CustomStream stream;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    protected CustomSubject subject;

    @ManyToOne
    @JoinColumn(name = "gender_specific_id")
    protected CustomGender genderSpecific;

    @Column(name = "selection_criteria")
    protected String selectionCriteria;

    @ManyToOne
    @JoinColumn(name = "sector_id")
    protected CustomSector sector;

    @Column(name = "notifying_authority")
    protected String notifyingAuthority;

    @NotNull
    @Column(name = "created_date")
    protected Date createdDate;

    @Column(name = "post_name")
    protected String postName;

}