package com.newmarket.garment.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ChatPartnerForm {

    @NotBlank
    private String buyerOrSeller;

    @NotBlank
    private String nickname;

}
