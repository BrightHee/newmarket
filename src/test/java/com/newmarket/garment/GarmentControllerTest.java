package com.newmarket.garment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newmarket.MockMvcTest;
import com.newmarket.account.Account;
import com.newmarket.account.AccountRepository;
import com.newmarket.account.AccountService;
import com.newmarket.account.WithAccount;
import com.newmarket.area.AreaRepository;
import com.newmarket.garment.form.CityCountryDistrictForm;
import com.newmarket.garment.form.CityProvinceForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class GarmentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private GarmentService garmentService;
    @Autowired private GarmentRepository garmentRepository;
    @Autowired private AreaRepository areaRepository;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AccountRepository accountRepository;

    private final String TEST_EMAIL = "test@email.com";

    private void verifyEmail() {
        Account account = accountRepository.findByEmail(TEST_EMAIL);
        account.finishSignUp();
    }

    @DisplayName("옷 판매 등록 화면 보이는지 확인")
    @WithAccount(TEST_EMAIL)
    @Test
    public void garmentForm() throws Exception {
        verifyEmail();
        mockMvc.perform(get("/new-garment"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("garment/new-garment"))
                .andExpect(model().attributeExists("account", "garmentForm", "cityProvinceList", "garmentType"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
    }

    @DisplayName("옷 판매 등록 화면에서 시/구 선택시 해당 시/군/구 보내는지 확인")
    @WithAccount(TEST_EMAIL)
    @Test
    public void sendCityCountryDistrictInfo() throws Exception {
        String cityProvince = "서울특별시";
        CityProvinceForm cityProvinceForm = new CityProvinceForm();
        cityProvinceForm.setCityProvince(cityProvince);
        String content = objectMapper.writeValueAsString(cityProvinceForm);

        List<String> cityCountryDistrictList = areaRepository.findDistinctCityCountryDistrict(cityProvince);

        String responseString = mockMvc.perform(post("/new-garment/cityCountryDistrict")
                    .characterEncoding("utf8")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(TEST_EMAIL))
                .andReturn().getResponse().getContentAsString();
        assertEquals(responseString, objectMapper.writeValueAsString(cityCountryDistrictList));
    }

    @DisplayName("옷 판매 등록 화면에서 시/구, 시/군/구 선택시 해당 읍/면/동 보내는지 확인")
    @WithAccount(TEST_EMAIL)
    @Test
    public void sendTownTownshipNeighborhood() throws Exception {
        String cityProvince = "서울특별시";
        String cityCountryDistrict = "광진구";
        CityCountryDistrictForm cityCountryDistrictForm = new CityCountryDistrictForm();
        cityCountryDistrictForm.setCityProvince(cityProvince);
        cityCountryDistrictForm.setCityCountryDistrict(cityCountryDistrict);
        String content = objectMapper.writeValueAsString(cityCountryDistrictForm);

        List<String> townTownshipNeighborhoodList = areaRepository.findTownTownshipNeighborhood(cityProvince, cityCountryDistrict);

        String responseString = mockMvc.perform(post("/new-garment/townTownshipNeighborhood")
                    .characterEncoding("utf8")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(TEST_EMAIL))
                .andReturn().getResponse().getContentAsString();
        assertEquals(responseString, objectMapper.writeValueAsString(townTownshipNeighborhoodList));
    }

    @DisplayName("옷 판매 등록 처리 - 성공")
    @WithAccount(TEST_EMAIL)
    @Test
    public void newGarment() throws Exception {
        verifyEmail();
        assertTrue(garmentRepository.findAll().isEmpty());

        mockMvc.perform(post("/new-garment")
                    .param("title", "테스트판매")
                    .param("content", "테스트입니다.")
                    .param("image", "")
                    .param("type", "VEST")
                    .param("price", "10000")
                    .param("cityProvince", "서울특별시")
                    .param("cityCountryDistrict", "광진구")
                    .param("townTownshipNeighborhood", "화양동")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/garments"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
        assertFalse(garmentRepository.findAll().isEmpty());
    }

    @DisplayName("옷 판매 등록 처리 - 실패")
    @WithAccount(TEST_EMAIL)
    @Test
    public void newGarment_fail() throws Exception {
        verifyEmail();
        // 없는 타입인 경우
        mockMvc.perform(post("/new-garment")
                    .param("title", "테스트판매")
                    .param("content", "테스트입니다.")
                    .param("image", "")
                    .param("type", "NOTYPE")
                    .param("price", "10000")
                    .param("cityProvince", "서울특별시")
                    .param("cityCountryDistrict", "광진구")
                    .param("townTownshipNeighborhood", "화양동")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("garment/new-garment"))
                .andExpect(model().attributeExists("account", "errorMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
        assertTrue(garmentRepository.findAll().isEmpty());

        // 없는 지역인 경우
        mockMvc.perform(post("/new-garment")
                    .param("title", "테스트판매")
                    .param("content", "테스트입니다.")
                    .param("image", "")
                    .param("type", "VEST")
                    .param("price", "10000")
                    .param("cityProvince", "서울특별시")
                    .param("cityCountryDistrict", "아무구")
                    .param("townTownshipNeighborhood", "아무동")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("garment/new-garment"))
                .andExpect(model().attributeExists("account", "errorMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
        assertTrue(garmentRepository.findAll().isEmpty());

        // 가격이 1000원 단위가 아닌 경우
        mockMvc.perform(post("/new-garment")
                    .param("title", "테스트판매")
                    .param("content", "테스트입니다.")
                    .param("image", "")
                    .param("type", "VEST")
                    .param("price", "12345")
                    .param("cityProvince", "서울특별시")
                    .param("cityCountryDistrict", "광진구")
                    .param("townTownshipNeighborhood", "화양동")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("garment/new-garment"))
                .andExpect(model().attributeExists("account", "errorMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
        assertTrue(garmentRepository.findAll().isEmpty());
    }

}