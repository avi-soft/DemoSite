package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddPhysicalRequirementDto {

    @JsonProperty("gender_id")
    Long genderId;
    @JsonProperty("height")
    Double height;
    @JsonProperty("weight")
    Double weight;
    @JsonProperty("shoe_size")
    Double shoeSize;
    @JsonProperty("waist_size")
    Double waistSize;
    @JsonProperty("chest_size")
    Double chestSize;

}
