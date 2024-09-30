package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateQualificationDto
{
    private Long id;

    @Pattern(regexp = "^(?!\\s*$).+", message = "Institution name cannot be blank")
    @Pattern(regexp = "^[^\\d]*$", message = "Institution name cannot contain numeric values")
    @Size(max = 255, message = "Institution name should not exceed 255 characters")
    private String institution_name;

//    @Min(value = 1900, message = "Year of passing should not be before 1900")
//    @Max(value = 9999, message = "Year of passing should be a valid 4-digit year")
    private Long year_of_passing;

    @Pattern(regexp = "^(?!\\s*$).+", message = "Board or University cannot be blank")
    @Pattern(regexp = "^[^\\d]*$", message = "Board or University cannot contain numeric values")
    @Size(max = 255, message = "Board or University name should not exceed 255 characters")
    private String board_or_university;

    @Pattern(regexp = "^(?!\\s*$).+", message = "Subject stream cannot be blank")
    @Pattern(regexp = "^[^\\d]*$", message = "Subject stream cannot contain numeric values")
    @Size(max = 255, message = "Subject stream should not exceed 255 characters")
    private String subject_stream;

    @Pattern(regexp = "^(?!\\s*$).+", message = "Grade or percentage value cannot be blank")
    @Pattern(regexp = "^(100|[1-9]?[0-9](\\\\.\\\\d*)?)$|^[A-Za-z]+$", message = "Grade or percentage value must be either a number  (up to 100) or a valid grade")
    @Size(max = 10, message = "Grade or percentage value should not exceed 10 characters")
    private String grade_or_percentage_value;

    @Min(value = 1, message = "Total marks must be greater than zero")
    private Long total_marks;

    @Min(value = 0, message = "Marks obtained cannot be negative")
    private Long marks_obtained;

    private Integer qualification_id;

    @AssertTrue(message = "Total marks cannot be less than marks obtained")
    private boolean isMarksTotalValid() {
        return total_marks >= marks_obtained;
    }

//    @AssertTrue(message = "Year of passing must be less than or equal to the current year")
//    private boolean isYearOfPassingValid() {
//        return year_of_passing <= Year.now().getValue();
//    }
}
