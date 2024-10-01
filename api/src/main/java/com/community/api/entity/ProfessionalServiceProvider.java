package com.community.api.entity;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class ProfessionalServiceProvider extends ServiceProviderEntity
{
    @Column(name = "business_unit_infra_score")
    private Integer business_unit_infra_score;

    @Column(name="work_experience_score")
    private Integer work_experience_score;

    @Column(name="qualification_score")
    private Integer qualification_score;

    @Column(name="technical_expertise_score")
    private Integer technical_expertise_score;

    @Column(name = "staff_score")
    private Integer staff_score;

    @Column(name="written_test_score")
    private Integer written_test_score;

    @Column(name = "image_upload_score")
    private Integer image_upload_score;

}