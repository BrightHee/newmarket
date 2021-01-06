package com.newmarket.modules.garment.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ChatRoomForm {

    @NotBlank
    private String buyerNickname;

    @NotBlank
    private String sellerNickname;

    @NotBlank
    private String message;

}
