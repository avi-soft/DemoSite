package com.community.api.endpoint.customer;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.Examination;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Entity
@Data
@NoArgsConstructor
public class Qualification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String institutionName;

    @Column(nullable = false)
    private int yearOfPassing;

    @Column(nullable = false)
    private String boardOrUniversity;

    @Column(nullable = false)
    private String subjectStream;

    @Column(nullable = false)
    private boolean isPercentage;

    @Column(nullable = false)
    private String gradeOrPercentageValue;

    @Column(nullable = false)
    private int marksTotal;
    @Column(nullable = false)
    private int marksObtained;
    private String examinationName;


    @ManyToOne
//    @JsonIgnore
    @JoinColumn(name = "custom_customer_id")
    private CustomCustomer customCustomer;
}
