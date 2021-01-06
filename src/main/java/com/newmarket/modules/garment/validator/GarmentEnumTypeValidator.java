package com.newmarket.modules.garment.validator;

import com.newmarket.modules.garment.annotation.GarmentEnumType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class GarmentEnumTypeValidator implements ConstraintValidator<GarmentEnumType, String> {

    private GarmentEnumType garmentEnumType;

    @Override
    public void initialize(GarmentEnumType constraintAnnotation) {
        this.garmentEnumType = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        Enum<?>[] enumValues = this.garmentEnumType.enumClass().getEnumConstants();
        if (enumValues != null) {
            for (Enum<?> enumValue : enumValues) {
                if (value.equals(enumValue.toString())) {
                    return true;
                }
            }
        }
        return false;
    }

}
