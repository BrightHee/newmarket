package com.newmarket.infra.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({"local", "test"})
@RequiredArgsConstructor
public class ConsoleEmailService implements EmailService {

    @Override
    public void sendEmail(EmailMessage emailMessage) {
        log.info("------------메시지------------");
        log.info("제목: {}", emailMessage.getSubject());
        log.info("보낸사람: {}", emailMessage.getTo());
        log.info("내용: {}", emailMessage.getMessage());
    }

}
