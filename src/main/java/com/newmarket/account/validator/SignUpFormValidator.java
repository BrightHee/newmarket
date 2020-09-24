package com.newmarket.account.validator;

import com.newmarket.account.AccountRepository;
import com.newmarket.account.form.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(SignUpForm.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        SignUpForm signUpForm = (SignUpForm) o;
        if (accountRepository.existsByNickname(signUpForm.getNickname())) {
            errors.rejectValue("nickname", "exist.nickname", new Object[]{signUpForm.getNickname()}, "이미 존재하는 닉네임입니다.");
        }
        if (accountRepository.existsByEmail(signUpForm.getEmail())) {
            errors.rejectValue("email", "exist.email", new Object[]{signUpForm.getEmail()}, "이미 존재하는 이메일입니다.");
        }
        if (!signUpForm.getPasswordAgain().equals(signUpForm.getPassword())) {
            errors.rejectValue("passwordAgain", "not_equal.password", new Object[]{signUpForm.getPasswordAgain()}, "비밀번호가 같지 않습니다.");
        }
    }
}
