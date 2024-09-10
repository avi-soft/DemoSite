package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
@Entity
@Table(name = "Qualification")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Qualification
{
    @Id
    private Long qualification_id;

    @NotBlank(message = "Qualification name is required")
    @Size(max = 255, message = "Qualification name should not exceed 255 characters")
    @Pattern(regexp = "^[^\\d]*$", message = "Qualification name cannot contain numeric values")
    @Column(name = "qualification_name", nullable = false)
    private String qualification_name;

    @NotBlank(message = "Qualification Description is required")
    @Size(max = 255, message = "Qualification description should not exceed 255 characters")
    @Pattern(regexp = "^[^\\d]*$", message = "Qualification description cannot contain numeric values")
    @Column(name = "qualification_description", nullable = false)
    private String qualification_description;
}

