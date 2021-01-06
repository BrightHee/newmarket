package com.newmarket.modules.account.validator;

import com.newmarket.modules.account.AccountRepository;
import com.newmarket.modules.account.form.PasswordForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class PasswordFormValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(PasswordForm.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        PasswordForm passwordForm = (PasswordForm) o;
        if (!passwordForm.getPasswordAgain().equals(passwordForm.getPassword())) {
            errors.rejectValue("passwordAgain", "not_equal.password", new Object[]{passwordForm.getPasswordAgain()}, "비밀번호가 같지 않습니다.");
        }
    }
}
