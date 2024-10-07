package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="custom_job_group")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomJobGroup {

    @Id
    @Column(name="job_group_id")
    @JsonProperty("job_group_id")
    protected Long jobGroupId;

    @Column(name="job_group")
    @JsonProperty("job_group")
    protected Character jobGroup;

    @Column(name="job_group_description")
    @JsonProperty("job_group_description")
    protected String jobDescription;

}
