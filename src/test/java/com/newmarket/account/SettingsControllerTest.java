package com.newmarket.account;

import com.newmarket.MockMvcTest;
import com.newmarket.account.form.PasswordForm;
import com.newmarket.account.form.ProfileForm;
import com.newmarket.account.form.SignUpForm;
import com.newmarket.mail.EmailMessage;
import com.newmarket.mail.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class SettingsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountFactory accountFactory;
    @Autowired AccountRepository accountRepository;
    @Autowired AccountService accountService;
    @Autowired PasswordEncoder passwordEncoder;
    @MockBean EmailService emailService;

    private final String TEST_EMAIL = "test@email.com";
    private final String TEST_PASSWORD = "abcd1234!";

    @DisplayName("프로필 수정 화면 보이는지 확인")
    @WithAccount(TEST_EMAIL)
    @Test
    public void profileForm() throws Exception {
        mockMvc.perform(get("/settings/profile"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/settings/profile"))
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
    }

    @DisplayName("프로필 수정 - 성공")
    @WithAccount(TEST_EMAIL)
    @Test
    public void updateProfile() throws Exception {
        ProfileForm profileForm = ProfileForm.builder()
                .nickname("테스트계정")  // 원래 닉네임으로 해도 프로필 수정됨
                .greetings("프로필을 수정했습니다~")
                .build();

        mockMvc.perform(post("/settings/profile")
                    .param("nickname", profileForm.getNickname())
                    .param("greetings", profileForm.getGreetings())
                    .param("profileImage", profileForm.getProfileImage())
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/profile"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        Account account = accountRepository.findByEmail(TEST_EMAIL);
        assertNotNull(account);
        assertEquals(account.getNickname(), profileForm.getNickname());
        assertEquals(account.getGreetings(), profileForm.getGreetings());
        assertEquals(account.getProfileImage(), profileForm.getProfileImage());
    }

    @DisplayName("프로필 수정 - 실패")
    @WithAccount(TEST_EMAIL)
    @Test
    public void updateProfile_fail() throws Exception {
        // 이미 있는 닉네임으로 바꾸는 경우
        accountFactory.createAccount("테스트계정2", "test2@email.com");

        ProfileForm profileForm = ProfileForm.builder()
                .nickname("테스트계정2")
                .greetings("프로필을 수정했습니다~")
                .build();

        mockMvc.perform(post("/settings/profile")
                    .param("nickname", profileForm.getNickname())
                    .param("greetings", profileForm.getGreetings())
                    .param("profileImage", profileForm.getProfileImage())
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/settings/profile"))
                .andExpect(model().hasErrors())
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        Account account = accountRepository.findByEmail(TEST_EMAIL);
        assertNotNull(account);
        assertNotEquals(account.getNickname(), profileForm.getNickname());
    }

    @DisplayName("비밀번호 확인 화면 보이는지 확인")
    @WithAccount(TEST_EMAIL)
    @Test
    public void passwordConfirmForm() throws Exception {
        mockMvc.perform(get("/settings/password-confirm"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/settings/password-confirm"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
    }

    @DisplayName("비밀번호 확인 - 성공")
    @WithAccount(TEST_EMAIL)
    @Test
    public void passwordConfirm() throws Exception {
        Account account = accountRepository.findByEmail(TEST_EMAIL);
        assertNotNull(account);
        assertFalse(account.isPasswordVerified());

        mockMvc.perform(post("/settings/password-confirm")
                    .param("password", "abcd1234!")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/password"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        assertTrue(account.isPasswordVerified());
    }

    @DisplayName("비밀번호 확인 - 틀린 비밀번호")
    @WithAccount(TEST_EMAIL)
    @Test
    public void passwordConfirm_with_wrong_password() throws Exception {
        Account account = accountRepository.findByEmail(TEST_EMAIL);
        assertNotNull(account);
        assertFalse(account.isPasswordVerified());

        mockMvc.perform(post("/settings/password-confirm")
                    .param("password", "!wrong123")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/settings/password-confirm"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        assertFalse(account.isPasswordVerified());
    }

    @DisplayName("비밀번호 변경 화면 보이는지 확인 - 성공")
    @WithAccount(TEST_EMAIL)
    @Test
    public void passwordForm() throws Exception {
        Account account = accountRepository.findByEmail(TEST_EMAIL);
        account.setPasswordVerified(true);
        accountRepository.save(account);
        accountRepository.flush();

        mockMvc.perform(get("/settings/password"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/settings/password"))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
    }

    @DisplayName("비밀번호 변경 화면 보이는지 확인 - 비밀번호 확인 없이 바로 접근해서 실패")
    @WithAccount(TEST_EMAIL)
    @Test
    public void passwordForm_with_no_passwordConfirm() throws Exception {
        mockMvc.perform(get("/settings/password"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/settings/password-confirm"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        // post 요청으로 접근해도 실패
        mockMvc.perform(post("/settings/password")
                    .param("password", "1234abc!")
                    .param("passwordAgain", "1234abc!")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/settings/password-confirm"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
    }

    @DisplayName("비밀번호 변경 - 성공")
    @WithAccount(TEST_EMAIL)
    @Test
    public void updatePassword() throws Exception {
        Account account = accountRepository.findByEmail(TEST_EMAIL);
        account.setPasswordVerified(true);
        accountRepository.save(account);
        accountRepository.flush();

        String newPassword = "new12345!";
        mockMvc.perform(post("/settings/password")
                    .param("password", newPassword)
                    .param("passwordAgain", newPassword)
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/password-confirm"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        assertFalse(account.isPasswordVerified());
        assertTrue(passwordEncoder.matches(newPassword, account.getPassword()));
    }

    @DisplayName("비밀번호 변경 - 다른 비밀번호 입력")
    @WithAccount(TEST_EMAIL)
    @Test
    public void passwordConfirm_not_equal() throws Exception {
        Account account = accountRepository.findByEmail(TEST_EMAIL);
        account.setPasswordVerified(true);
        accountRepository.save(account);
        accountRepository.flush();

        mockMvc.perform(post("/settings/password")
                    .param("password", "new12345!")
                    .param("passwordAgain", "new12345@")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/settings/password"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        assertTrue(account.isPasswordVerified());
        assertTrue(passwordEncoder.matches(TEST_PASSWORD, account.getPassword()));  // 바뀌지 않음
    }

    @DisplayName("이메일 인증 재전송 화면 보이는지 확인")
    @WithAccount(TEST_EMAIL)
    @Test
    public void resendEmailCertificationForm() throws Exception {
        mockMvc.perform(get("/settings/resend-email-certification"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/settings/resend-email-certification"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
    }

    @DisplayName("이메일 인증 재전송 - 성공")
    @WithAccount(TEST_EMAIL)
    @Test
    public void resendEmailCertification() throws Exception {
        Account account = accountRepository.findByEmail(TEST_EMAIL);
        String oldToken = account.getCertificationToken();

        account.setCertificationTokenGeneratedLocalDateTime(LocalDateTime.now().minusMinutes(11));
        accountRepository.save(account);
        accountRepository.flush();

        mockMvc.perform(post("/settings/resend-email-certification")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/resend-email-certification"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        then(emailService).should(times(2)).sendEmail(any(EmailMessage.class));  // 회원 가입 때 1번 + 재전송 때 1번
        assertNotEquals(oldToken, account.getCertificationToken());
    }

    @DisplayName("이메일 인증 재전송 - 실패(쿨타임)")
    @WithAccount(TEST_EMAIL)
    @Test
    public void resendEmailCertification_fail() throws Exception {
        mockMvc.perform(post("/settings/resend-email-certification")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/settings/resend-email-certification"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
    }

    @DisplayName("알림 설정 화면 보이는지 확인")
    @WithAccount(TEST_EMAIL)
    @Test
    public void notificationForm() throws Exception {
        mockMvc.perform(get("/settings/notification"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/settings/notification"))
                .andExpect(model().attributeExists("notificationForm"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
    }

    @DisplayName("알림 설정 처리")
    @WithAccount(TEST_EMAIL)
    @Test
    public void updateNotificationSettings() throws Exception {
        mockMvc.perform(post("/settings/notification")
                    .param("sentSellerChatMessages", "false")
                    .param("sentBuyerChatMessages", "true")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/notification"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        Account account = accountRepository.findByEmail(TEST_EMAIL);
        assertEquals(account.isSentSellerChatMessages(), false);
        assertEquals(account.isSentBuyerChatMessages(), true);
    }

}
