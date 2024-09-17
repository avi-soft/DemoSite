package com.community.api.dto;

import com.broadleafcommerce.rest.api.wrapper.ProductWrapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class AddCategoryDto {

    @JsonProperty("name")
    protected String name;
    @JsonProperty("description")
    protected String description;
    @JsonProperty("long_description")
    protected String longDescription;
    @JsonProperty("url")
    protected String url;
    @JsonProperty("url_key")
    protected String urlKey;
    @JsonProperty("active_start_date")
    protected Date activeStartDate;
    @JsonProperty("active_end_date")
    protected Date activeEndDate;
    @JsonProperty("display_template")
    protected String displayTemplate;
}
