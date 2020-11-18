package com.newmarket.garment.form;

import com.newmarket.garment.GarmentType;
import com.newmarket.garment.annotation.GarmentEnumType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarmentForm {

    @NotBlank
    @Pattern(
            regexp = "^[ㄱ-ㅎ가-힣\\p{Graph}\\s]{1,50}$",
            message = "50자 이내로 입력하세요.")
    private String title;

    private String content;

    private String image;

    @NotBlank
    @GarmentEnumType(enumClass = GarmentType.class)
    private String type;

    @Min(value = 1000, message = "범위를 벗어나는 값입니다.")
    @Max(value = Integer.MAX_VALUE, message = "범위를 벗어나는 값입니다.")
    private Integer price;

    @NotBlank
    private String cityProvince;

    @NotBlank
    private String cityCountryDistrict;

    @NotBlank
    private String townTownshipNeighborhood;

}
