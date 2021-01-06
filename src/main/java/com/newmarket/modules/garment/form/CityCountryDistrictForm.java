package com.newmarket.modules.garment.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CityCountryDistrictForm {

    @NotBlank
    private String cityProvince;

    @NotBlank
    private String cityCountryDistrict;

}
