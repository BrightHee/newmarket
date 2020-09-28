package com.newmarket.mail;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EmailMessage {

    private String to;

    private String subject;

    private String message;

}
