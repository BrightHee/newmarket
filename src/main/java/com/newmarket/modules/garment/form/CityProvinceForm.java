package com.newmarket.modules.garment.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CityProvinceForm {

    @NotBlank
    private String cityProvince;

}
