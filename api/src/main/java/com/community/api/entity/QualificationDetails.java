package com.community.api.entity;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "qualification_details")
public class QualificationDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Institution name is required")
    @Size(max = 255, message = "Institution name should not exceed 255 characters")
    @Pattern(regexp = "^[^\\d]*$", message = "Institution name cannot contain numeric values")
    @Column(name = "institution_name", nullable = false)
    private String institution_name;

//    @Min(value = 1900, message = "Year of passing should not be before 1900")
//    @Max(value = 9999, message = "Year of passing should be a valid 4-digit year")
    @NotNull(message = "Year of passing is required")
    @Column(name = "year_of_passing", nullable = false)
    private Long year_of_passing;

    @NotBlank(message = "Board or University is required")
    @Size(max = 255, message = "Board or University name should not exceed 255 characters")
    @Pattern(regexp = "^[^\\d]*$", message = "Board or University cannot contain numeric values")
    @Column(name = "board_or_university", nullable = false)
    private String board_or_university;

    @NotNull(message = "Examination Role Number is required")
    @Column(name = "examination_role_number", nullable = false)
    private Long examination_role_number;

    @NotNull(message = "Examination Registration Number is required")
    @Column(name = "examination_registration_number")
    private Long examination_registration_number;

    @NotBlank(message = "Subject name is required")
    @Size(max = 255, message = "Subject name should not exceed 255 characters")
    @Pattern(regexp = "^[^\\d]*$", message = "Subject name cannot contain numeric values")
    @Column(name = "subject_name", nullable = false)
    private String subject_name;

    @NotBlank(message = "Stream is required")
    @Size(max = 255, message = "Stream should not exceed 255 characters")
    @Pattern(regexp = "^[^\\d]*$", message = "Stream cannot contain numeric values")
    @Column(name = "stream", nullable = false)
    private String stream;

    @NotBlank(message = "Grade or percentage value is required")
    @Pattern(regexp = "^(100|[1-9]?[0-9](\\\\.\\\\d*)?)$|^[A-Za-z]+$", message = "Grade or percentage value must be either a number  (up to 100) or a valid grade")
    @Size(max = 10, message = "Grade or percentage value should not exceed 10 characters")
    @Column(name = "grade_or_percentage_value", nullable = false)
    private String grade_or_percentage_value;

    @Min(value = 1, message = "Total marks must be greater than zero")
    @Column(name = "total_marks", nullable = false)
    private Long total_marks;

    @Min(value = 0, message = "Marks obtained cannot be negative")
    @Column(name = "marks_obtained", nullable = false)
    private Long marks_obtained;

    @NotNull(message = "Qualification id is required")
    @Column(name = "qualification_id", nullable = false)
    private Integer qualification_id;

    @AssertTrue(message = "Total marks cannot be less than marks obtained")
    private Boolean isMarksTotalValid() {
        return total_marks >= marks_obtained;
    }

//    @AssertTrue(message = "Year of passing must be less than or equal to the current year")
//    private boolean isYearOfPassingValid() {
//        return year_of_passing <= Year.now().getValue();
//    }
    @JsonBackReference("qualificationDetailsList-customer")
    @ManyToOne
    @JoinColumn(name = "custom_customer_id")
    private CustomCustomer custom_customer;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "service_provider_id")
    private ServiceProviderEntity service_provider;

}
