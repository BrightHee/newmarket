package com.newmarket.modules.chatRoom.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageForm {

    @NotBlank
    private String nickname;

    private boolean me;

    @NotBlank
    private String message;

    @NotBlank
    private LocalDateTime sentDateTime;

}
