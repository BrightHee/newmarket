package com.newmarket.garment.validator;

import com.newmarket.account.AccountRepository;
import com.newmarket.garment.form.ChatPartnerForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class ChatPartnerFormValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(ChatPartnerForm.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        ChatPartnerForm chatPartnerForm = (ChatPartnerForm) o;

        if (!chatPartnerForm.getBuyerOrSeller().matches("^buyer|seller$")) {
            errors.rejectValue("buyerOrSeller", "wrong_type", new Object[]{chatPartnerForm.getBuyerOrSeller()}, "구매자나 판매자가 아닙니다.");
        }
        if (!accountRepository.existsByNickname(chatPartnerForm.getNickname())) {
            errors.rejectValue("nickname", "no_nickname", new Object[]{chatPartnerForm.getNickname()}, "없는 닉네임입니다.");
        }
    }
}
