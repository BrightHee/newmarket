package com.newmarket.account.validator;

import com.newmarket.account.AccountRepository;
import com.newmarket.account.form.ProfileForm;
import com.newmarket.account.form.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class ProfileFormValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(ProfileForm.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        ProfileForm profileForm = (ProfileForm) o;
        if (accountRepository.existsByNickname(profileForm.getNickname())) {
            errors.rejectValue("nickname", "exist.nickname", new Object[]{profileForm.getNickname()}, "이미 존재하는 닉네임입니다.");
        }
    }
}
