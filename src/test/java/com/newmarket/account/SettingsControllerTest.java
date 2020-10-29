package com.newmarket.account;

import com.newmarket.MockMvcTest;
import com.newmarket.account.form.PasswordForm;
import com.newmarket.account.form.ProfileForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class SettingsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;

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
                .nickname("바뀐닉네임")
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
        ProfileForm profileForm = ProfileForm.builder()
                .nickname("테스트계정")
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
        assertNotEquals(account.getGreetings(), profileForm.getGreetings());
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

}
