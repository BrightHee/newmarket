package com.newmarket.account;

import com.newmarket.MockMvcTest;
import com.newmarket.account.form.SignUpForm;
import com.newmarket.mail.EmailMessage;
import com.newmarket.mail.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class AccountControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AccountService accountService;
    @Autowired private AccountRepository accountRepository;
    @MockBean private EmailService emailService;

    @DisplayName("회원 가입 화면 보이는지 확인")
    @Test
    public void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"));
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
                .andExpect(redirectedUrl("/"));

        Account account = accountRepository.findByEmail("test@email.com");
        assertNotNull(account);
        assertNotEquals(account.getPassword(), "abcd1234!");  // 패스워드 인코딩 확인
        assertNotNull(account.getCertificationToken());
        then(emailService).should(times(1)).sendEmail(any(EmailMessage.class));  // 이메일 보내는지 확인
        // TODO: 자동 로그인 확인
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
                .andExpect(model().hasErrors());

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
                .andExpect(model().attribute("email", savedAccount.getEmail()));

        assertTrue(savedAccount.isEmailVerified());
        assertNotNull(savedAccount.getJoinedDateTime());
        // TODO: 자동 로그인 확인
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
                .andExpect(model().attribute("error", "not_exist.email"));
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
                .andExpect(model().attribute("error", "invalid.token"));

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
                .andExpect(model().attribute("error", "invalid.token"));
    }

}