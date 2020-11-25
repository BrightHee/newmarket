package com.newmarket;

import com.newmarket.account.Account;
import com.newmarket.account.AccountFactory;
import com.newmarket.account.AccountRepository;
import com.newmarket.account.WithAccount;
import com.newmarket.garment.Garment;
import com.newmarket.garment.GarmentFactory;
import com.newmarket.garment.GarmentRepository;
import com.newmarket.mail.EmailMessage;
import com.newmarket.mail.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class MainControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AccountFactory accountFactory;
    @Autowired private AccountRepository accountRepository;
    @Autowired private GarmentFactory garmentFactory;
    @Autowired private GarmentRepository garmentRepository;

    private final String TEST_EMAIL = "test@email.com";

    @DisplayName("검색기능 확인")
    @WithAccount(TEST_EMAIL)
    @Test
    public void search() throws Exception {
        accountFactory.verifyEmail(TEST_EMAIL);
        Account account = accountRepository.findByEmail(TEST_EMAIL);

        garmentFactory.createGarment(33, account,
                "서울특별시", "광진구", "화양동");
        garmentFactory.createGarment(33, account,
                "부산광역시", "사하구", "구평동");
        garmentFactory.createGarment(34, account,
                "대전광역시", "서구", "내동");

        String keywords = "서울 코트 대전 서구";
        MvcResult result = mockMvc.perform(get("/search")
                    .param("keywords", keywords))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("search"))
                .andExpect(model().attributeExists("currentGarments", "sortProperty", "keywords"))
                .andReturn();

        Page page = (Page) result.getModelAndView().getModel().get("currentGarments");
        assertEquals(page.getSize(), 20);
        assertEquals(page.getTotalElements(), 67);
        assertEquals(page.getTotalPages(), 4);
        assertEquals(page.getNumber(), 0);

        for (int i = 0; i < page.getContent().size(); i++) {  // 최근 데이터 20개 조회
            Garment garment = (Garment) page.getContent().get(i);
            assertEquals(garment.getArea().getCityProvince(), "대전광역시");  // 대전이 가장 나중에 등록되므로
        }
    }

}