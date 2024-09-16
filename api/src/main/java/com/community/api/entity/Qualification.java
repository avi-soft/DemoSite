package com.community.api.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.Year;

@Entity
@Data
@NoArgsConstructor
public class Qualification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Institution name is required")
    @Size(max = 255, message = "Institution name should not exceed 255 characters")
    @Pattern(regexp = "^[^\\d]*$", message = "Institution name cannot contain numeric values")
    @Column(name = "institution_name", nullable = false)
    private String institutionName;

    @Min(value = 1900, message = "Year of passing should not be before 1900")
    @Max(value = 9999, message = "Year of passing should be a valid 4-digit year")
    @Column(name = "year_of_passing", nullable = false)
    private int yearOfPassing;

    @NotBlank(message = "Board or University is required")
    @Size(max = 255, message = "Board or University name should not exceed 255 characters")
    @Pattern(regexp = "^[^\\d]*$", message = "Board or University cannot contain numeric values")
    @Column(name = "board_or_university", nullable = false)
    private String boardOrUniversity;

    @NotBlank(message = "Subject stream is required")
    @Size(max = 255, message = "Subject stream should not exceed 255 characters")
    @Pattern(regexp = "^[^\\d]*$", message = "Subject stream cannot contain numeric values")
    @Column(name = "subject_stream", nullable = false)
    private String subjectStream;


    @NotBlank(message = "Grade or percentage value is required")
    @Pattern(regexp = "\\d+\\.?\\d*|[A-F]|[a-f]", message = "Grade or percentage value must be either a number or a valid grade")
    @Size(max = 10, message = "Grade or percentage value should not exceed 10 characters")
    @Column(name = "grade_or_percentage_value", nullable = false)
    private String gradeOrPercentageValue;

    @Min(value = 1, message = "Total marks must be greater than zero")
    @Column(name = "total_marks", nullable = false)
    private int marksTotal;

    @Min(value = 0, message = "Marks obtained cannot be negative")
    @Column(name = "marks_obtained", nullable = false)
    private int marksObtained;

    @NotBlank(message = "Examination name is required")
    @Size(max = 255, message = "Examination name should not exceed 255 characters")
    @Pattern(regexp = "^[^\\d]*$", message = "Examination name cannot contain numeric values")
    @Column(name = "examination_name", nullable = false)
    private String examinationName;

    @AssertTrue(message = "Total marks cannot be less than marks obtained")
    private boolean isMarksTotalValid() {
        return marksTotal >= marksObtained;
    }

    @AssertTrue(message = "Year of passing must be less than or equal to the current year")
    private boolean isYearOfPassingValid() {
        return yearOfPassing <= Year.now().getValue();
    }
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "custom_customer_id")
    private CustomCustomer customCustomer;


}
