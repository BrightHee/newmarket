package com.newmarket.modules.account;

import com.newmarket.modules.account.form.NotificationForm;
import com.newmarket.modules.account.form.ProfileForm;
import com.newmarket.modules.account.form.SignUpForm;
import com.newmarket.infra.config.AppProperties;
import com.newmarket.infra.mail.EmailMessage;
import com.newmarket.infra.mail.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final TemplateEngine templateEngine;
    private final EmailService emailService;
    private final AppProperties appProperties;

    public Account signUp(SignUpForm signUpForm) {
        Account account = saveNewAccount(signUpForm);
        sendSignUpEmailCertification(account);
        return account;
    }

    private void sendSignUpEmailCertification(Account account) {
        account.createCertificationToken();
        sendEmailCertification(account, "뉴마켓의 신규회원 가입에 감사드립니다. 뉴마켓의 서비스를 이용하시려면 아래의 링크를 클릭해서 이메일 인증을 진행해 주십시오.");
    }

    private void sendEmailCertification(Account account, String content) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("message", content);
        context.setVariable("linkMessage", "이메일 인증하기");
        context.setVariable("link", appProperties.getHost()
                + "/check-certification-token?token=" + account.getCertificationToken()
                + "&email=" + account.getEmail());
        String message = templateEngine.process("mail/simple-link-mail", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("뉴마켓 이메일 인증받기")
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
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    public void finishSignUp(Account account) {
        account.finishSignUp();
        login(account);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(username);
        if (account == null) {
            throw new UsernameNotFoundException(username);
        }
        return new UserAccount(account);
    }

    public void changePassword(Account account) {
        account.getNewPassword();
        sendNewPassword(account);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        accountRepository.save(account);
    }

    private void sendNewPassword(Account account) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("message", "뉴마켓의 새로운 비밀번호입니다. 새로운 비밀번호로 로그인한 후에 반드시 비밀번호 변경을 하시기 바랍니다.");
        context.setVariable("password", account.getPassword());
        String message = templateEngine.process("mail/simple-link-mail", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("뉴마켓 새 비밀번호 확인")
                .message(message)
                .build();
        emailService.sendEmail(emailMessage);
    }

    public void updateProfile(Account account, ProfileForm profileForm) {
        account.setNickname(profileForm.getNickname());
        account.setGreetings(profileForm.getGreetings());
        account.setProfileImage(profileForm.getProfileImage());
        accountRepository.save(account);
    }

    public void updatePassword(Account account, String password) {
        account.setPassword(passwordEncoder.encode(password));
        accountRepository.save(account);
    }

    public void resendEmailCertification(Account account) {
        account.createCertificationToken();
        accountRepository.save(account);
        sendEmailCertification(account, "뉴마켓의 인증 메일입니다. 뉴마켓의 서비스를 이용하시려면 아래의 링크를 클릭해서 이메일 인증을 진행해 주십시오.");
    }

    public void updateNotificationSettings(Account account, NotificationForm notificationForm) {
        account.setSentSellerChatMessages(notificationForm.isSentSellerChatMessages());
        account.setSentBuyerChatMessages(notificationForm.isSentBuyerChatMessages());
        accountRepository.save(account);
    }
}
