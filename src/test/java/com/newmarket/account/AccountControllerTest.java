package com.newmarket.account;

import com.newmarket.MockMvcTest;
import com.newmarket.account.form.SignUpForm;
import com.newmarket.mail.EmailMessage;
import com.newmarket.mail.EmailService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class AccountControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AccountService accountService;
    @Autowired private AccountRepository accountRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @MockBean private EmailService emailService;

    @DisplayName("회원 가입 화면 보이는지 확인")
    @Test
    public void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(unauthenticated());
    }

    @DisplayName("회원 가입 처리 - 입력값 정상")
    @Test
    public void signUp() throws Exception {
        mockMvc.perform(post("/sign-up")
                    .param("nickname", "테스트계정")
                    .param("email", "test@email.com")
                    .param("password", "abcd1234!")
                    .param("passwordAgain", "abcd1234!")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("test@email.com"));

        Account account = accountRepository.findByEmail("test@email.com");
        assertNotNull(account);
        assertNotEquals(account.getPassword(), "abcd1234!");  // 패스워드 인코딩 확인
        assertNotNull(account.getCertificationToken());
        then(emailService).should(times(1)).sendEmail(any(EmailMessage.class));  // 이메일 보내는지 확인
    }

    @DisplayName("회원 가입 처리 - 입력값 오류")
    @Test
    public void signUp_with_wrong_value() throws Exception {
        mockMvc.perform(post("/sign-up")
                    .param("nickname", "테스트계정")
                    .param("email", "test@email.com")
                    .param("password", "abcd1234")  // 특수 문자 제외
                    .param("passwordAgain", "abcd1234")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().hasErrors())
                .andExpect(unauthenticated());

        assertNull(accountRepository.findByEmail("test@email.com"));
        then(emailService).shouldHaveNoInteractions();
    }

    @DisplayName("이메일 인증 처리 - 토큰, 이메일 정상")
    @Test
    public void checkCertificationToken() throws Exception {
        Account account = Account.builder()
                .nickname("테스트계정")
                .email("test@email.com")
                .password("abcd1234!")
                .certificationToken("test-token")
                .certificationTokenGeneratedLocalDateTime(LocalDateTime.now())
                .build();
        Account savedAccount = accountRepository.save(account);

        mockMvc.perform(get("/check-certification-token")
                    .param("token", savedAccount.getCertificationToken())
                    .param("email", savedAccount.getEmail()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/check-certification-token"))
                .andExpect(model().attribute("nickname", savedAccount.getNickname()))
                .andExpect(model().attribute("email", savedAccount.getEmail()))
                .andExpect(authenticated().withUsername("test@email.com"));

        assertTrue(savedAccount.isEmailVerified());
        assertNotNull(savedAccount.getJoinedDateTime());
    }

    @DisplayName("이메일 인증 처리 - 존재하지 않는 이메일")
    @Test
    public void checkCertificationToken_with_no_email() throws Exception {
        assertNull(accountRepository.findByEmail("no@email.com"));
        mockMvc.perform(get("/check-certification-token")
                    .param("token", "test-token")
                    .param("email", "no@email.com"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/check-certification-token"))
                .andExpect(model().attribute("error", "not_exist.email"))
                .andExpect(unauthenticated());
    }

    @DisplayName("이메일 인증 처리 - 잘못된 토큰")
    @Test
    public void checkCertificationToken_with_invalid_token() throws Exception {
        Account account = Account.builder()
                .nickname("테스트계정")
                .email("test@email.com")
                .password("abcd1234!")
                .certificationToken("test-token")
                .certificationTokenGeneratedLocalDateTime(LocalDateTime.now())
                .build();
        Account savedAccount = accountRepository.save(account);

        // 다른 토큰 보냄
        mockMvc.perform(get("/check-certification-token")
                    .param("token", "invalid-token")
                    .param("email", savedAccount.getEmail()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/check-certification-token"))
                .andExpect(model().attribute("error", "invalid.token"))
                .andExpect(unauthenticated());

        // 유효기간이 지난 토큰 보냄
        savedAccount.setCertificationTokenGeneratedLocalDateTime(LocalDateTime.now().minusMinutes(11));
        accountRepository.save(savedAccount);
        accountRepository.flush();

        mockMvc.perform(get("/check-certification-token")
                    .param("token", savedAccount.getCertificationToken())
                    .param("email", savedAccount.getEmail()))
                    .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/check-certification-token"))
                .andExpect(model().attribute("error", "invalid.token"))
                .andExpect(unauthenticated());
    }

    @DisplayName("비밀번호 찾기 화면 보이는지 확인")
    @Test
    public void findPasswordForm() throws Exception {
        mockMvc.perform(get("/find-password"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/find-password"))
                .andExpect(unauthenticated());
    }

    @DisplayName("비밀번호 찾기 처리 - 성공")
    @Test
    public void findPassword() throws Exception {
        Account account = Account.builder()
                .nickname("테스트계정")
                .email("test@email.com")
                .password(passwordEncoder.encode("abcd1234!"))
                .emailVerified(true)
                .build();
        Account savedAccount = accountRepository.save(account);
        String oldPassword = savedAccount.getPassword();

        mockMvc.perform(post("/find-password")
                    .param("email", savedAccount.getEmail())
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(unauthenticated());

        assertNotEquals(oldPassword, savedAccount.getPassword());
        then(emailService).should(times(1)).sendEmail(any(EmailMessage.class));  // 이메일 보내는지 확인
    }

    @DisplayName("비밀번호 찾기 처리 - 가입하지 않은 이메일, 인증받지 않은 이메일")
    @Test
    public void findPassword_fail() throws Exception {
        // 가입하지 않은 이메일
        mockMvc.perform(post("/find-password")
                    .param("email", "no@email.com")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/find-password"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(unauthenticated());

        then(emailService).shouldHaveNoInteractions();

        Account account = Account.builder()
                .nickname("테스트계정")
                .email("test@email.com")
                .password(passwordEncoder.encode("abcd1234!"))
                .build();
        Account savedAccount = accountRepository.save(account);
        String oldPassword = savedAccount.getPassword();

        // 인증받지 않은 이메일
        mockMvc.perform(post("/find-password")
                    .param("email", savedAccount.getEmail())
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/find-password"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(unauthenticated());

        assertEquals(oldPassword, savedAccount.getPassword());
        then(emailService).shouldHaveNoInteractions();
    }

}