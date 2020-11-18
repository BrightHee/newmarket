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
import com.newmarket.garment.form.GarmentForm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.util.NestedServletException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

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

    @DisplayName("전체 옷(페이징) 화면 보이는지 확인")
    @WithAccount(TEST_EMAIL)
    @Test
    public void showGarments() throws Exception {
        verifyEmail();
        createDummyGarment(100, accountRepository.findByEmail(TEST_EMAIL));
        MvcResult result = mockMvc.perform(get("/garments"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("garment/garments"))
                .andExpect(model().attributeExists("currentGarments")).andReturn();

        Page page = (Page) result.getModelAndView().getModel().get("currentGarments");
        assertEquals(page.getSize(), 20);
        assertEquals(page.getTotalElements(), 100);
        assertEquals(page.getTotalPages(), 5);
        assertEquals(page.getNumber(), 0);

        for (int i = 0; i < page.getContent().size(); i++) {
            Garment garment = (Garment) page.getContent().get(i);
            assertEquals(garment.getTitle(), "제목(" + (100-i) + ")");
        }
    }

    private void createDummyGarment(int num, Account account) {
        GarmentForm garmentForm = new GarmentForm();
        for (int i = 0; i < num; i++) {
            garmentForm.setTitle("제목(" + (i+1) + ")");
            garmentForm.setType(GarmentType.CARDIGAN.toString());
            garmentForm.setPrice(20000);
            garmentForm.setCityProvince("서울특별시");
            garmentForm.setCityCountryDistrict("광진구");
            garmentForm.setTownTownshipNeighborhood("화양동");
            garmentForm.setContent("내용~~!");
            garmentService.addNewGarment(garmentForm, account);
        }
    }

    @DisplayName("상세 옷 화면 보이는지 확인")
    @WithAccount(TEST_EMAIL)
    @Test
    public void showDetails() throws Exception {
        verifyEmail();
        createDummyGarment(10, accountRepository.findByEmail(TEST_EMAIL));
        long id = garmentRepository.findByTitle("제목(3)").get(0).getId();
        MvcResult result = mockMvc.perform(get("/garment/" + id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("garment/details"))
                .andExpect(model().attributeExists("garment")).andReturn();
        Garment garment = (Garment) result.getModelAndView().getModel().get("garment");
        assertEquals(garment.getTitle(), "제목(3)");
        assertEquals(garment.getAccount(), accountRepository.findByEmail(TEST_EMAIL));
    }

    @DisplayName("옷 게시글 관리 화면 보이는지 확인")
    @WithAccount(TEST_EMAIL)
    @Test
    public void manageGarments() throws Exception {
        verifyEmail();
        createDummyGarment(5, accountRepository.findByEmail(TEST_EMAIL));

        // 1개는 판매 종료된 상태
        Garment garment = garmentRepository.findByTitle("제목(3)").get(0);
        garment.setClosed(true);
        garmentRepository.save(garment);

        MvcResult result = mockMvc.perform(get("/garments/management"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("garment/management"))
                .andExpect(model().attributeExists("account", "garments"))
                .andExpect(authenticated().withUsername(TEST_EMAIL)).andReturn();
        List<Garment> garments = (List<Garment>) result.getModelAndView().getModel().get("garments");
        assertEquals(garments.size(), 4);
    }

    @DisplayName("옷 게시글 수정 화면 보이는지 확인")
    @WithAccount(TEST_EMAIL)
    @Test
    public void updateGarmentForm() throws Exception {
        verifyEmail();
        createDummyGarment(5, accountRepository.findByEmail(TEST_EMAIL));
        Garment garment = garmentRepository.findByTitle("제목(3)").get(0);

        mockMvc.perform(get("/garment/" + garment.getId() + "/update"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("garment/update"))
                .andExpect(model().attributeExists("account", "garmentForm", "garmentType", "cityProvinceList",
                        "cityCountryDistrictList", "townTownshipNeighborhoodList"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
    }

    @DisplayName("옷 게시글 수정 화면 접근 실패 - 없는 게시글")
    @WithAccount(TEST_EMAIL)
    @Test
    public void updateGarment_not_exist() throws Exception {
        NestedServletException exception = assertThrows(NestedServletException.class, () -> {
            mockMvc.perform(get("/garment/" + 1 + "/update"))
                    .andDo(print())
                    .andExpect(status().is4xxClientError())
                    .andExpect(view().name("garment/management"))
                    .andExpect(model().attributeExists("account", "errorMessage"))
                    .andExpect(authenticated().withUsername(TEST_EMAIL));
        });
        assertTrue(exception.getCause().toString().contains("NoSuchElementException"));
    }

    @DisplayName("옷 게시글 수정 화면 접근 실패 - 판매종료된 게시글, 작성자와 다른 아이디로 접근")
    @WithAccount(TEST_EMAIL)
    @Test
    public void updateGarment_wrong_access() throws Exception {
        verifyEmail();
        createDummyGarment(1, accountRepository.findByEmail(TEST_EMAIL));
        Garment garment = garmentRepository.findByTitle("제목(1)").get(0);
        garment.setClosed(true);
        garmentRepository.save(garment);
        garmentRepository.flush();

        // 판매종료된 게시글
        mockMvc.perform(get("/garment/" + garment.getId() + "/update"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("garment/management"))
                .andExpect(model().attributeExists("account", "errorMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        Account account = Account.builder()
                .nickname("테스트계정2")
                .email("test2@email.com")
                .password("abcd1234!")
                .certificationToken("test-token")
                .certificationTokenGeneratedLocalDateTime(LocalDateTime.now())
                .build();
        Account savedAccount = accountRepository.save(account);
        createDummyGarment(1, savedAccount);
        Garment otherGarment = garmentRepository.findByAccountAndClosed(savedAccount, false).get(0);

        // 작성자와 다른 아이디
        mockMvc.perform(get("/garment/" + otherGarment.getId() + "/update"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("garment/management"))
                .andExpect(model().attributeExists("account", "errorMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
    }

    @DisplayName("옷 게시글 수정 - 성공")
    @WithAccount(TEST_EMAIL)
    @Test
    public void updateGarment() throws Exception {
        verifyEmail();
        createDummyGarment(5, accountRepository.findByEmail(TEST_EMAIL));
        Garment garment = garmentRepository.findByTitle("제목(3)").get(0);

        mockMvc.perform(post("/garment/" + garment.getId() + "/update")
                    .param("title", "새로운 내용의 제목")
                    .param("content", garment.getContent())
                    .param("image", garment.getImage())
                    .param("type", garment.getType().toString())
                    .param("price", garment.getPrice().toString())
                    .param("cityProvince", garment.getArea().getCityProvince())
                    .param("cityCountryDistrict", garment.getArea().getCityCountryDistrict())
                    .param("townTownshipNeighborhood", garment.getArea().getTownTownshipNeighborhood())
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/garments/management"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        assertTrue(garment.getTitle().equals("새로운 내용의 제목"));
    }

    @DisplayName("옷 게시글 수정 - 실패")
    @WithAccount(TEST_EMAIL)
    @Test
    public void updateGarment_fail() throws Exception {
        verifyEmail();
        createDummyGarment(5, accountRepository.findByEmail(TEST_EMAIL));
        Garment garment = garmentRepository.findByTitle("제목(3)").get(0);

        // 없는 지역인 경우
        mockMvc.perform(post("/garment/" + garment.getId() + "/update")
                        .param("title", "새로운 내용의 제목")
                        .param("content", garment.getContent())
                        .param("image", garment.getImage())
                        .param("type", garment.getType().toString())
                        .param("price", garment.getPrice().toString())
                        .param("cityProvince", "서울특별시")
                        .param("cityCountryDistrict", "아무구")
                        .param("townTownshipNeighborhood", "아무동")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("garment/update"))
                .andExpect(model().attributeExists("account", "errorMessage", "garmentType", "cityProvinceList",
                        "cityCountryDistrictList", "townTownshipNeighborhoodList"))
                .andExpect(model().hasErrors())
                .andExpect(authenticated().withUsername(TEST_EMAIL));
        assertTrue(garment.getTitle().equals("제목(3)"));
    }

    @DisplayName("옷 게시글 삭제 - 성공")
    @WithAccount(TEST_EMAIL)
    @Test
    public void deleteGarment() throws Exception {
        verifyEmail();
        createDummyGarment(5, accountRepository.findByEmail(TEST_EMAIL));
        Garment garment = garmentRepository.findByTitle("제목(3)").get(0);

        mockMvc.perform(post("/garment/" + garment.getId() + "/delete")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/garments/management"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        assertTrue(garmentRepository.findById(garment.getId()).isEmpty());
    }

}