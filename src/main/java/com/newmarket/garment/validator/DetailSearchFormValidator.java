package com.newmarket.garment.validator;

import com.newmarket.garment.form.DetailSearchForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DetailSearchFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(DetailSearchForm.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        DetailSearchForm detailSearchForm = (DetailSearchForm) o;
        if (!Arrays.stream(new String[] {"전체", "판매중"}).anyMatch(detailSearchForm.getClosed()::equals)) {
            errors.rejectValue("closed", "no_value", new Object[]{detailSearchForm.getClosed()}, "없는 값입니다.");
        }
        if (!Arrays.stream(new String[] {"오늘", "이번주", "1개월", "6개월"}).anyMatch(detailSearchForm.getDuration()::equals)) {
            errors.rejectValue("duration", "no_value", new Object[]{detailSearchForm.getDuration()}, "없는 값입니다.");
        }
        // area 검증은 추가 쿼리를 많이 발생시키기 때문에 안함
    }
}
