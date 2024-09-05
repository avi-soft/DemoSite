package com.community.api.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.CodePointLength;

import javax.persistence.*;

@Entity
@Table(name="typing_text")
@Data
@NoArgsConstructor
public class TypingText
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text",nullable = false, columnDefinition = "TEXT")
    private String text;
}
