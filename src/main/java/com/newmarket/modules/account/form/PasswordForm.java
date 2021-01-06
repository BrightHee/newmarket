package com.newmarket.modules.account.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class PasswordForm {

    @NotBlank
    @Pattern(
            regexp = "^(?=.*?\\p{Alpha})(?=.*?\\p{Digit})(?=.*?\\p{Punct}).{8,30}$",
            message = "영문자 + 숫자 + 특수기호를 조합하여 8자 이상 30자 이내로 입력하세요.")
    @Length(min = 8, max = 30)
    private String password;

    @NotBlank
    @Pattern(
            regexp = "^(?=.*?\\p{Alpha})(?=.*?\\p{Digit})(?=.*?\\p{Punct}).{8,30}$",
            message = "영문자 + 숫자 + 특수기호를 조합하여 8자 이상 30자 이내로 입력하세요.")
    @Length(min = 8, max = 30)
    private String passwordAgain;

}
