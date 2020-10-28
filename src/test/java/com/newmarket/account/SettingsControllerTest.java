package com.newmarket.account;

import com.newmarket.MockMvcTest;
import com.newmarket.account.form.ProfileForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final String TEST_EMAIL = "test@email.com";

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
}
