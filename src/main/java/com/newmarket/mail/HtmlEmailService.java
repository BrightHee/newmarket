package com.newmarket.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Slf4j
@Component
@Profile({"dev"})
@RequiredArgsConstructor
public class HtmlEmailService implements EmailService {

    private final JavaMailSender javaMailSender;

    @Override
    public void sendEmail(EmailMessage emailMessage) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            message.setTo(emailMessage.getTo());
            message.setSubject(emailMessage.getSubject());
            message.setText(emailMessage.getMessage(), true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("이메일 전송 실패", e);
            // TODO: 관리자가 처리하도록 관리자에게 메일 보내기
            // TODO: 사용자에게 에러 화면 보여주기(회원가입이 rollback 되지 않도록 async로 mail service 구현)
            throw new RuntimeException(e);
        }
    }

}
