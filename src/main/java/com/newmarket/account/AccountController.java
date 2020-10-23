package com.newmarket.account;

import com.newmarket.account.annotation.AuthenticatedAccount;
import com.newmarket.account.form.SignUpForm;
import com.newmarket.account.validator.SignUpFormValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final SignUpFormValidator signUpFormValidator;

    @InitBinder("signUpForm")
    public void validateSignUpForm(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute("signUpForm", new SignUpForm());
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUp(@Valid SignUpForm signUpForm, Errors errors) {
        if (errors.hasErrors()) {
            return "account/sign-up";
        }
        Account account = accountService.signUp(signUpForm);
        accountService.login(account);
        return "redirect:/";
    }

    @GetMapping("/check-certification-token")
    public String checkCertificationToken(@AuthenticatedAccount Account account, String token, String email, Model model) {
        if (account != null) {
            model.addAttribute("account", account);
        }
        Account byEmail = accountRepository.findByEmail(email);
        if (byEmail == null) {
            model.addAttribute("error", "not_exist.email");
            return "account/check-certification-token";
        }
        if (!byEmail.isValidToken(token)) {
            model.addAttribute("error", "invalid.token");
            return "account/check-certification-token";
        }
        accountService.finishSignUp(byEmail);
        model.addAttribute("nickname", byEmail.getNickname());
        model.addAttribute("email", byEmail.getEmail());
        return "account/check-certification-token";
    }

    @GetMapping("/find-password")
    public String findPasswordForm() {
        return "account/find-password";
    }

    @PostMapping("/find-password")
    public String findPassword(String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        if (account == null) {
            model.addAttribute("error", "가입하지 않은 이메일입니다.");
            return "account/find-password";
        }
        if (!account.isEmailVerified()) {
            model.addAttribute("error", "인증받지 않은 이메일은 비밀번호를 변경하실 수 없습니다.");
            return "account/find-password";
        }
        accountService.changePassword(account);
        return "redirect:/login";
    }

}
