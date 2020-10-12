package com.newmarket.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Slf4j
@Component
@Profile({"test"})
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
