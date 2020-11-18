package com.newmarket.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AccountFactory {

    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    public Account createAccount(String nickname, String email) {
        Account account = Account.builder()
                .nickname(nickname)
                .email(email)
                .password(passwordEncoder.encode("abcd1234!"))
                .certificationToken("test-token")
                .certificationTokenGeneratedLocalDateTime(LocalDateTime.now())
                .build();
        return accountRepository.save(account);
    }

    public void verifyEmail(String email) {
        Account account = accountRepository.findByEmail(email);
        account.finishSignUp();
    }

}
