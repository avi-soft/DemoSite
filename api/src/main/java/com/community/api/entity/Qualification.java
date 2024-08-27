package com.community.api.entity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
public class Qualification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_name", nullable = false)
    private String institutionName;

    @Column(name ="year_of_passing", nullable = false)
    private int yearOfPassing;

    @Column(name ="board_or_university", nullable = false)
    private String boardOrUniversity;

    @Column(name = "subject_stream",nullable = false)
    private String subjectStream;

    @Column(name = "is_percentage", nullable = false)
    private boolean isPercentage;

    @Column(name = "grade_or_percentage_value",nullable = false)
    private String gradeOrPercentageValue;

    @Column(name = "total_marks",nullable = false)
    private int marksTotal;
    @Column(name = "marks_obtained", nullable = false)
    private int marksObtained;
    @Column(name = "examination_name", nullable = false)
    private String examinationName;


    @ManyToOne
//    @JsonIgnore
    @JoinColumn(name = "custom_customer_id")
    private CustomCustomer customCustomer;
}
