package com.community.api.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
@Entity
@Table(name = "Examination")
@Data
@NoArgsConstructor

public class Examination
{
    @Id
    private Long examination_id;

    @NotBlank(message = "Examination name is required")
    @Size(max = 255, message = "Examination name should not exceed 255 characters")
    @Pattern(regexp = "^[^\\d]*$", message = "Examination name cannot contain numeric values")
    @Column(name = "examination_name", nullable = false)
    private String examination_name;
}
