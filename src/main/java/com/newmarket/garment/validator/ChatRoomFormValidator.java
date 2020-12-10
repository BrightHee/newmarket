package com.newmarket.garment.validator;

import com.newmarket.account.AccountRepository;
import com.newmarket.garment.form.ChatRoomForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class ChatRoomFormValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(ChatRoomForm.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        ChatRoomForm chatRoomForm = (ChatRoomForm) o;

        if (!accountRepository.existsByNickname(chatRoomForm.getBuyerNickname())) {
            errors.rejectValue("buyerNickname", "no_nickname", new Object[]{chatRoomForm.getBuyerNickname()}, "없는 닉네임입니다.");
        }
        if (!accountRepository.existsByNickname(chatRoomForm.getSellerNickname())) {
            errors.rejectValue("sellerNickname", "no_nickname", new Object[]{chatRoomForm.getSellerNickname()}, "없는 닉네임입니다.");
        }
    }
}
