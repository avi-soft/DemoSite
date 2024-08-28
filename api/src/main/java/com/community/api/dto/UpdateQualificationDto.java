package com.community.api.dto;

import com.community.api.entity.CustomCustomer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.*;
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateQualificationDto
{
    private Long id;

    @Pattern(regexp = "^(?!\\s*$).+", message = "Institution name cannot be blank")
    @Size(max = 255, message = "Institution name should not exceed 255 characters")
    private String institutionName;

    @Min(value = 1900, message = "Year of passing should not be before 1900")
    @Max(value = 9999, message = "Year of passing should be a valid 4-digit year")
    @Column(name = "year_of_passing", nullable = false)
    private Integer yearOfPassing;

    @Pattern(regexp = "^(?!\\s*$).+", message = "Board or University cannot be blank")
    @Size(max = 255, message = "Board or University name should not exceed 255 characters")
    private String boardOrUniversity;

    @Pattern(regexp = "^(?!\\s*$).+", message = "Subject stream cannot be blank")
    @Size(max = 255, message = "Subject stream should not exceed 255 characters")
    private String subjectStream;

    private boolean isPercentage;

    @Pattern(regexp = "^(?!\\s*$).+", message = "Grade or percentage value cannot be blank")
    @Pattern(regexp = "\\d+\\.?\\d*|[A-F]|[a-f]", message = "Grade or percentage value must be either a number or a valid grade")
    @Size(max = 10, message = "Grade or percentage value should not exceed 10 characters")
    private String gradeOrPercentageValue;

    @Min(value = 1, message = "Total marks must be greater than zero")
    private Integer marksTotal;

    @Min(value = 0, message = "Marks obtained cannot be negative")
    private Integer marksObtained;

    @Pattern(regexp = "^(?!\\s*$).+", message = "Examination name cannot be blank")
    @Size(max = 255, message = "Examination name should not exceed 255 characters")
    private String examinationName;

    @AssertTrue(message = "Total marks cannot be less than marks obtained")
    private boolean isMarksTotalValid() {
        return marksTotal >= marksObtained;
    }

}
