package com.newmarket.modules.account.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Lob;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileForm {

    @NotBlank
    @Pattern(
            regexp = "^[가-힣\\p{Alnum}]{2,20}$",
            message = "공백없이 글자 및 숫자로 2자이상 20자이내로 입력하세요.")
    private String nickname;

    @Length(max = 50)
    private String greetings;

    private String profileImage;

}
