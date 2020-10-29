package com.newmarket.account;

import com.newmarket.account.annotation.AuthenticatedAccount;
import com.newmarket.account.form.PasswordForm;
import com.newmarket.account.form.ProfileForm;
import com.newmarket.account.validator.PasswordFormValidator;
import com.newmarket.account.validator.ProfileFormValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final ProfileFormValidator profileFormValidator;
    private final PasswordEncoder passwordEncoder;
    private final PasswordFormValidator passwordFormValidator;

    @InitBinder("profileForm")
    public void validateProfileForm(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(profileFormValidator);
    }

    @InitBinder("passwordForm")
    public void validatePasswordForm(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(passwordFormValidator);
    }

    // 개인 정보 설정이기 때문에 모든 컨트롤러에서 model로 account 추가
    @ModelAttribute("account")
    public Account account(@AuthenticatedAccount Account account) {
        return account;
    }

    @GetMapping("/profile")
    public String profileForm(@AuthenticatedAccount Account account, Model model) {
        ProfileForm profileForm = ProfileForm.builder()
                .nickname(account.getNickname())
                .greetings(account.getGreetings())
                .profileImage(account.getProfileImage())
                .build();
        model.addAttribute("profileForm", profileForm);
        return "account/settings/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid ProfileForm profileForm, Errors errors,
                                @AuthenticatedAccount Account account, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            return "account/settings/profile";
        }
        accountService.updateProfile(account, profileForm);
        attributes.addFlashAttribute("successMessage", "프로필을 업데이트 했습니다.");
        return "redirect:/settings/profile";
    }

    @GetMapping("/password-confirm")
    public String passwordConfirmForm() {
        return "account/settings/password-confirm";
    }

    @PostMapping("/password-confirm")
    public String passwordConfirm(String password, @AuthenticatedAccount Account account, Model model,
                                  RedirectAttributes attributes) {
        if (!passwordEncoder.matches(password, account.getPassword())) {
            model.addAttribute("errorMessage", "비밀번호가 틀렸습니다.");
            return "account/settings/password-confirm";
        }
        account.setPasswordVerified(true);
        attributes.addFlashAttribute("successMessage", "비밀번호를 확인했습니다.");
        return "redirect:/settings/password";
    }

    @GetMapping("/password")
    public String passwordForm(@AuthenticatedAccount Account account, Model model) {
        if (!account.isPasswordVerified()) {
            model.addAttribute("errorMessage", "비밀번호를 확인해야 변경이 가능합니다.");
            return "account/settings/password-confirm";
        }
        model.addAttribute("passwordForm", new PasswordForm());
        return "account/settings/password";
    }

    @PostMapping("/password")
    public String updatePassword(@Valid PasswordForm passwordForm, Errors errors,
                                 @AuthenticatedAccount Account account, Model model, RedirectAttributes attributes) {
        if (!account.isPasswordVerified()) {
            model.addAttribute("errorMessage", "비밀번호를 확인해야 변경이 가능합니다.");
            return "account/settings/password-confirm";
        }
        if (errors.hasErrors()) {
            model.addAttribute("errorMessage", "새 비밀번호 설정에 실패했습니다.");
            return "account/settings/password";
        }
        accountService.updatePassword(account, passwordForm.getPassword());
        account.setPasswordVerified(false);
        attributes.addFlashAttribute("successMessage", "새 비밀번호를 설정했습니다.");
        return "redirect:/settings/password-confirm";
    }

}
