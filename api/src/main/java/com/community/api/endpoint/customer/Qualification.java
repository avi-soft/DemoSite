package com.community.api.endpoint.customer;
import com.community.api.entity.CustomCustomer;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
public class Qualification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String examination_name;

    @Column(nullable = false)
    private String institute_name;

    @Column(nullable = false)
    private int year_of_passing;

    @Column(nullable = false)
    private String board_or_university;

    @Column(nullable = false)
    private String subject_stream;

    @Column(nullable = false)
    private boolean is_percentage;

    @Column(nullable = false)
    private String grade_or_percentage;

    @Column(nullable = false)
    private int total_marks;
    @Column(nullable = false)
    private int obtained_marks;

    @ManyToOne
    @JoinColumn(name = "custom_customer_id")
    private CustomCustomer customCustomer;
}
