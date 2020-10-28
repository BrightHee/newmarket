package com.newmarket.account;

import com.newmarket.account.annotation.AuthenticatedAccount;
import com.newmarket.account.form.ProfileForm;
import com.newmarket.account.validator.ProfileFormValidator;
import lombok.RequiredArgsConstructor;
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

    @InitBinder("profileForm")
    public void validateProfileForm(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(profileFormValidator);
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

}
