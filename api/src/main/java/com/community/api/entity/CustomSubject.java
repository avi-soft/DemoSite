package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "custom_subject")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subject_id")
    protected Long subjectId;

    @Column(name = "subject_name")
    protected String subjectName;

    @Column(name = "subject_description")
    protected String subjectDescription;
}
