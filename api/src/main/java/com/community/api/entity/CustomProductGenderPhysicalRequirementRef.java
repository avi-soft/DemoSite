package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "custom_product_gender_physical_requirement_reference")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomProductGenderPhysicalRequirementRef {

    @Id
    @Column(name = "product_gender_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long productGenderId;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "product_id")
    protected CustomProduct customProduct;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "gender_id")
    protected CustomGender customGender;

    @NotNull
    @Column(name = "height")
    Double height;

    @NotNull
    @Column(name = "weight")
    Double weight;

    @Column(name = "shoe_size")
    Double shoeSize;

    @Column(name = "waist_size")
    Double waistSize;

    @Column(name = "chest_size")
    Double chestSize;

}
