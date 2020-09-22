package com.newmarket.account;

import com.newmarket.account.form.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private AccountRepository accountRepository;

    public Account saveNewAccount(SignUpForm signUpForm) {
        Account account = new Account();
        account.setEmail(signUpForm.getEmail());
        account.setNickname(signUpForm.getNickname());
        account.setPassword(signUpForm.getPassword()); // encoding 필요
        return accountRepository.save(account);
    }

    public void login(Account account) {
    }
}
