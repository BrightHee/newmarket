package com.newmarket.modules.garment.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class DetailSearchForm {

    @NotBlank
    private String closed = "전체";

    @NotBlank
    private String duration = "이번주";

    @NotBlank
    private String cityProvince = "전체";

    @NotBlank
    private String cityCountryDistrict = "전체";

    @NotBlank
    private String townTownshipNeighborhood = "전체";

}
