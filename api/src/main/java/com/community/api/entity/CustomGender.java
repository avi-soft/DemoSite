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
@Table(name="custom_gender")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomGender {
    @Id
    @Column(name="gender_id")
    @JsonProperty("gender_id")
    protected Long genderId;

    @Column(name="gender_symbol")
    @JsonProperty("gender_symbol")
    protected Character genderSymbol;

    @Column(name="gender_name")
    @JsonProperty("gender_name")
    protected String genderName;
}
