package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "scoring_criteria")
public class ScoringCriteria {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "attribute_name")
    private String attribute_name;
    
    @Column(name = "condition")
    private String condition;
    
    @Column(name = "score")
    private Integer score;


}
