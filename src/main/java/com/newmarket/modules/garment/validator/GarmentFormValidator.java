package com.newmarket.modules.garment.validator;

import com.newmarket.modules.area.AreaRepository;
import com.newmarket.modules.garment.form.GarmentForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class GarmentFormValidator implements Validator {

    private final AreaRepository areaRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(GarmentForm.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        GarmentForm garmentForm = (GarmentForm) o;
        if ((garmentForm.getPrice() % 1000) != 0) {
            errors.rejectValue("price", "wrong_price", new Object[]{garmentForm.getPrice()}, "1000원 단위여야 합니다.");
        }
        if (!areaRepository.existsByCityProvinceAndCityCountryDistrictAndTownTownshipNeighborhood(garmentForm.getCityProvince(),
                garmentForm.getCityCountryDistrict(), garmentForm.getTownTownshipNeighborhood())) {
            errors.rejectValue("cityProvince", "no_area", new Object[]{garmentForm.getCityProvince()}, "없는 지역입니다.");
        }
    }
}
