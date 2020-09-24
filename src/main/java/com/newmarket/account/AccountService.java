package com.newmarket.account;

import com.newmarket.account.form.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Account signUp(SignUpForm signUpForm) {
        Account account = saveNewAccount(signUpForm);
        // 인증 이메일 보내기
        return account;
    }

    private Account saveNewAccount(SignUpForm signUpForm) {
        Account account = new Account();
        account.setEmail(signUpForm.getEmail());
        account.setNickname(signUpForm.getNickname());
        account.setPassword(signUpForm.getPassword()); // encoding 필요
        return accountRepository.save(account);
    }

    public void login(Account account) {
    }
}
