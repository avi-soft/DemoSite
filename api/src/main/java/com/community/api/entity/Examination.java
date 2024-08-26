package com.community.api.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long examination_id;

    @NotNull(message = "Name of examination cannot be null")
    @Size(min=1,max = 50,message = "min character is 1 and maximum characters can be upto 50")
    @Pattern(regexp = "^[^\\s].*$", message = "Invalid examination name")
    private String examination_name;
}
