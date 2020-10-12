package com.newmarket.account;

import com.newmarket.account.form.SignUpForm;
import com.newmarket.config.AppProperties;
import com.newmarket.mail.EmailMessage;
import com.newmarket.mail.EmailService;
import com.newmarket.mail.HtmlEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final TemplateEngine templateEngine;
    private final EmailService emailService;
    private final AppProperties appProperties;

    public Account signUp(SignUpForm signUpForm) {
        Account account = saveNewAccount(signUpForm);
        account.createCertificationToken();
        sendSignUpEmailCertificationToken(account);
        return account;
    }

    private void sendSignUpEmailCertificationToken(Account account) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("message", "뉴마켓의 신규회원 가입에 감사드립니다. 뉴마켓의 서비스를 이용하시려면 아래의 링크를 클릭해서 이메일 인증을 진행해 주십시오.");
        context.setVariable("linkMessage", "이메일 인증하기");
        context.setVariable("link", appProperties.getHost()
                + "/check-certification-token?token=" + account.getCertificationToken()
                + "&email=" + account.getEmail());
        String message = templateEngine.process("mail/simple-link-mail", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("뉴마켓 가입 이메일 인증받기")
                .message(message)
                .build();
        emailService.sendEmail(emailMessage);
    }

    private Account saveNewAccount(SignUpForm signUpForm) {
        Account account = Account.builder()
                .nickname(signUpForm.getNickname())
                .email(signUpForm.getEmail())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .build();
        return accountRepository.save(account);
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                account.getNickname(),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    public void finishSignUp(Account account) {
        account.finishSignUp();
        login(account);
    }

}
