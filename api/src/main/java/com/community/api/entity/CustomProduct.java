package com.community.api.entity;

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

    @Column(name = "notifying_authority")
    protected String notifyingAuthority;

    @Column(name = "advertiser_url")
    String advertiserUrl;

    @Column(name = "domicile_required")
    Boolean domicileRequired;

    @Column(name = "modifier_user_id")
    Long modifierUserId;

    @ManyToOne
    @JoinColumn(name = "modifier_role_id")
    Role modifierRole;

    @OneToOne
    @JoinColumn(name = "rejection_status_id")
    CustomProductRejectionStatus rejectionStatus;
}