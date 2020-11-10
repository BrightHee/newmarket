package com.newmarket.garment.form;

import com.newmarket.area.Area;
import com.newmarket.garment.GarmentType;
import com.newmarket.garment.annotation.GarmentEnumType;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class GarmentForm {

    @NotBlank
    @Pattern(
            regexp = "^[ㄱ-ㅎ가-힣\\p{Graph}]{1,80}$",
            message = "공백없이 80자 이내로 입력하세요.")
    private String title;

    private String content;

    private String image;

    @NotBlank
    @GarmentEnumType(enumClass = GarmentType.class)
    private String type;

    @Min(0)
    private Integer price;

    @NotBlank
    private String cityProvince;

    @NotBlank
    private String cityCountryDistrict;

    @NotBlank
    private String townTownshipNeighborhood;

}
