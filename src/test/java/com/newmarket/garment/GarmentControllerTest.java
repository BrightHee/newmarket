package com.newmarket.garment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newmarket.MockMvcTest;
import com.newmarket.account.*;
import com.newmarket.area.AreaRepository;
import com.newmarket.chatRoom.ChatRoom;
import com.newmarket.chatRoom.ChatRoomRepository;
import com.newmarket.chatRoom.ChatRoomService;
import com.newmarket.chatRoom.form.MessageForm;
import com.newmarket.garment.form.*;
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
    @Autowired private GarmentFactory garmentFactory;
    @Autowired private GarmentService garmentService;
    @Autowired private GarmentRepository garmentRepository;
    @Autowired private AreaRepository areaRepository;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AccountFactory accountFactory;
    @Autowired private AccountRepository accountRepository;
    @Autowired private ChatRoomService chatRoomService;
    @Autowired private ChatRoomRepository chatRoomRepository;

    private final String TEST_EMAIL = "test@email.com";
    private final String TEST_EMAIL2 = "test2@email.com";
    private final String CITY_PROVINCE = "서울특별시";
    private final String CITY_COUNTRY_DISTRICT = "광진구";
    private final String TOWN_TOWNSHIP_NEIGHBORHOOD = "화양동";

    @DisplayName("옷 판매 등록 화면 보이는지 확인")
    @WithAccount(TEST_EMAIL)
    @Test
    public void garmentForm() throws Exception {
        accountFactory.verifyEmail(TEST_EMAIL);
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
        CityProvinceForm cityProvinceForm = new CityProvinceForm();
        cityProvinceForm.setCityProvince(CITY_PROVINCE);
        String content = objectMapper.writeValueAsString(cityProvinceForm);

        String responseString = mockMvc.perform(post("/garment/area/cityCountryDistrict")
                    .characterEncoding("utf8")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(TEST_EMAIL))
                .andReturn().getResponse().getContentAsString();

        List<String> cityCountryDistrictList = areaRepository.findDistinctCityCountryDistrict(CITY_PROVINCE);
        assertEquals(responseString, objectMapper.writeValueAsString(cityCountryDistrictList));
    }

    @DisplayName("옷 판매 등록 화면에서 시/구, 시/군/구 선택시 해당 읍/면/동 보내는지 확인")
    @WithAccount(TEST_EMAIL)
    @Test
    public void sendTownTownshipNeighborhood() throws Exception {
        CityCountryDistrictForm cityCountryDistrictForm = new CityCountryDistrictForm();
        cityCountryDistrictForm.setCityProvince(CITY_PROVINCE);
        cityCountryDistrictForm.setCityCountryDistrict(CITY_COUNTRY_DISTRICT);
        String content = objectMapper.writeValueAsString(cityCountryDistrictForm);

        String responseString = mockMvc.perform(post("/garment/area/townTownshipNeighborhood")
                    .characterEncoding("utf8")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(TEST_EMAIL))
                .andReturn().getResponse().getContentAsString();

        List<String> townTownshipNeighborhoodList = areaRepository.findTownTownshipNeighborhood(CITY_PROVINCE, CITY_COUNTRY_DISTRICT);
        assertEquals(responseString, objectMapper.writeValueAsString(townTownshipNeighborhoodList));
    }

    @DisplayName("옷 판매 등록 처리 - 성공")
    @WithAccount(TEST_EMAIL)
    @Test
    public void newGarment() throws Exception {
        accountFactory.verifyEmail(TEST_EMAIL);
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
                .andExpect(flash().attributeExists("successMessage", "detailSearchForm"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
        assertFalse(garmentRepository.findAll().isEmpty());
    }

    @DisplayName("옷 판매 등록 처리 - 실패")
    @WithAccount(TEST_EMAIL)
    @Test
    public void newGarment_fail() throws Exception {
        accountFactory.verifyEmail(TEST_EMAIL);
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
                .andExpect(model().attributeExists("account", "errorMessage", "garmentType", "cityProvinceList"))
                .andExpect(model().hasErrors())
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
                .andExpect(model().attributeExists("account", "errorMessage", "garmentType", "cityProvinceList"))
                .andExpect(model().hasErrors())
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
                .andExpect(model().attributeExists("account", "errorMessage", "garmentType", "cityProvinceList"))
                .andExpect(model().hasErrors())
                .andExpect(authenticated().withUsername(TEST_EMAIL));
        assertTrue(garmentRepository.findAll().isEmpty());
    }

    @DisplayName("전체 옷(페이징) 화면 보이는지 확인")
    @WithAccount(TEST_EMAIL)
    @Test
    public void showGarments() throws Exception {
        accountFactory.verifyEmail(TEST_EMAIL);
        garmentFactory.createGarment(100, accountRepository.findByEmail(TEST_EMAIL),
                CITY_PROVINCE, CITY_COUNTRY_DISTRICT, TOWN_TOWNSHIP_NEIGHBORHOOD);
        MvcResult result = mockMvc.perform(get("/garments"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("garment/garments"))
                .andExpect(model().attributeExists("currentGarments", "sortProperty", "detailSearchForm", "cityProvinceList"))
                .andReturn();

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

    @DisplayName("전체 옷(페이징) 화면 보이는지 확인 - 잘못된 검색 값 전달")
    @WithAccount(TEST_EMAIL)
    @Test
    public void showGarments_with_wrong_value() throws Exception {
        accountFactory.verifyEmail(TEST_EMAIL);
        garmentFactory.createGarment(100, accountRepository.findByEmail(TEST_EMAIL),
                CITY_PROVINCE, CITY_COUNTRY_DISTRICT, TOWN_TOWNSHIP_NEIGHBORHOOD);
        mockMvc.perform(get("/garments")
                    .param("closed", "")
                    .param("duration", "없는값"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("garment/garments"))
                .andExpect(model().attributeExists("errorMessage", "detailSearchForm", "cityProvinceList"))
                .andExpect(model().hasNoErrors());  // 기본 detailSearchForm을 model에 추가
    }

    @DisplayName("상세 옷 화면 보이는지 확인")
    @WithAccount(TEST_EMAIL)
    @Test
    public void showDetails() throws Exception {
        accountFactory.verifyEmail(TEST_EMAIL);
        garmentFactory.createGarment(5, accountRepository.findByEmail(TEST_EMAIL),
                CITY_PROVINCE, CITY_COUNTRY_DISTRICT, TOWN_TOWNSHIP_NEIGHBORHOOD);

        long id = garmentRepository.findByTitle("제목(3)").get(0).getId();
        MvcResult result = mockMvc.perform(get("/garment/" + id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("garment/details"))
                .andExpect(model().attributeExists("garment", "chatRoomList")).andReturn();

        Garment garment = (Garment) result.getModelAndView().getModel().get("garment");
        assertEquals(garment.getTitle(), "제목(3)");
        assertEquals(garment.getAccount(), accountRepository.findByEmail(TEST_EMAIL));
    }

    @DisplayName("옷 게시글 관리 화면 보이는지 확인")
    @WithAccount(TEST_EMAIL)
    @Test
    public void manageGarments() throws Exception {
        accountFactory.verifyEmail(TEST_EMAIL);
        garmentFactory.createGarment(5, accountRepository.findByEmail(TEST_EMAIL),
                CITY_PROVINCE, CITY_COUNTRY_DISTRICT, TOWN_TOWNSHIP_NEIGHBORHOOD);

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
        accountFactory.verifyEmail(TEST_EMAIL);
        garmentFactory.createGarment(5, accountRepository.findByEmail(TEST_EMAIL),
                CITY_PROVINCE, CITY_COUNTRY_DISTRICT, TOWN_TOWNSHIP_NEIGHBORHOOD);
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
        accountFactory.verifyEmail(TEST_EMAIL);
        garmentFactory.createGarment(1, accountRepository.findByEmail(TEST_EMAIL),
                CITY_PROVINCE, CITY_COUNTRY_DISTRICT, TOWN_TOWNSHIP_NEIGHBORHOOD);

        // 판매종료된 게시글
        Garment garment = garmentRepository.findByTitle("제목(1)").get(0);
        garment.setClosed(true);
        garmentRepository.save(garment);
        garmentRepository.flush();

        mockMvc.perform(get("/garment/" + garment.getId() + "/update"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("garment/management"))
                .andExpect(model().attributeExists("account", "errorMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        Account account = accountFactory.createAccount("테스트계정2", "test2@email.com");
        garmentFactory.createGarment(1, account, CITY_PROVINCE, CITY_COUNTRY_DISTRICT, TOWN_TOWNSHIP_NEIGHBORHOOD);
        Garment otherGarment = garmentRepository.findByAccountAndClosed(account, false).get(0);

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
        accountFactory.verifyEmail(TEST_EMAIL);
        garmentFactory.createGarment(5, accountRepository.findByEmail(TEST_EMAIL),
                CITY_PROVINCE, CITY_COUNTRY_DISTRICT, TOWN_TOWNSHIP_NEIGHBORHOOD);
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
        accountFactory.verifyEmail(TEST_EMAIL);
        garmentFactory.createGarment(5, accountRepository.findByEmail(TEST_EMAIL),
                CITY_PROVINCE, CITY_COUNTRY_DISTRICT, TOWN_TOWNSHIP_NEIGHBORHOOD);
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
        accountFactory.verifyEmail(TEST_EMAIL);
        garmentFactory.createGarment(5, accountRepository.findByEmail(TEST_EMAIL),
                CITY_PROVINCE, CITY_COUNTRY_DISTRICT, TOWN_TOWNSHIP_NEIGHBORHOOD);
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

    @DisplayName("채팅방에서 채팅 보내지고 화면에 표시되는지 확인")
    @WithAccount(TEST_EMAIL)
    @Test
    public void sendChatMessage() throws Exception {
        accountFactory.createAccount("테스트계정2", TEST_EMAIL2);
        accountFactory.verifyEmail(TEST_EMAIL);
        accountFactory.verifyEmail(TEST_EMAIL2);
        Account me = accountRepository.findByEmail(TEST_EMAIL);
        Account partner = accountRepository.findByEmail(TEST_EMAIL2);

        // 내가 구매자인 경우
        garmentFactory.createGarment(1, partner,
                CITY_PROVINCE, CITY_COUNTRY_DISTRICT, TOWN_TOWNSHIP_NEIGHBORHOOD);
        Garment garment = garmentRepository.findByTitle("제목(1)").get(0);

        chatRoomService.findOrCreateNew(me, partner, "seller", garment);
        ChatRoomForm chatRoomForm = new ChatRoomForm();
        chatRoomForm.setSellerNickname(partner.getNickname());
        chatRoomForm.setBuyerNickname(me.getNickname());
        chatRoomForm.setMessage("안녕하세요~!");

        String responseString = mockMvc.perform(post("/garment/" + garment.getId() + "/chat")
                    .characterEncoding("utf8")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(chatRoomForm))
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json; charset=utf-8"))
                .andExpect(authenticated().withUsername(TEST_EMAIL))
                .andReturn().getResponse().getContentAsString();

        MessageForm responseContent = objectMapper.readValue(responseString, MessageForm.class);
        assertEquals(responseContent.getNickname(), me.getNickname());
        assertEquals(responseContent.getMessage(), "안녕하세요~!");
        assertEquals(responseContent.isMe(), true);

        ChatRoom chatRoom = chatRoomRepository.findBySellerAndBuyerAndGarment(partner, me, garment);
        assertEquals(chatRoom.getChatList().get(0).getSender().getNickname(), me.getNickname());
        assertEquals(chatRoom.getChatList().get(0).getMessage(), "안녕하세요~!");

        // 내가 판매자인 경우
        garmentFactory.createGarment(2, me,
                CITY_PROVINCE, CITY_COUNTRY_DISTRICT, TOWN_TOWNSHIP_NEIGHBORHOOD);
        Garment garment2 = garmentRepository.findByTitle("제목(2)").get(0);

        chatRoomService.findOrCreateNew(me, partner, "buyer", garment2);
        ChatRoomForm chatRoomForm2 = new ChatRoomForm();
        chatRoomForm2.setSellerNickname(me.getNickname());
        chatRoomForm2.setBuyerNickname(partner.getNickname());
        chatRoomForm2.setMessage("테스트메시지입니다.");

        String responseString2 = mockMvc.perform(post("/garment/" + garment2.getId() + "/chat")
                    .characterEncoding("utf8")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(chatRoomForm2))
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json; charset=utf-8"))
                .andExpect(authenticated().withUsername(TEST_EMAIL))
                .andReturn().getResponse().getContentAsString();

        MessageForm responseContent2 = objectMapper.readValue(responseString2, MessageForm.class);
        assertEquals(responseContent2.getNickname(), me.getNickname());
        assertEquals(responseContent2.getMessage(), "테스트메시지입니다.");
        assertEquals(responseContent2.isMe(), true);

        ChatRoom chatRoom2 = chatRoomRepository.findBySellerAndBuyerAndGarment(me, partner, garment2);
        assertEquals(chatRoom2.getChatList().get(0).getSender().getNickname(), me.getNickname());
        assertEquals(chatRoom2.getChatList().get(0).getMessage(), "테스트메시지입니다.");
    }

    @DisplayName("채팅방 입장시 없으면 채팅방 만들고 있으면 이전 채팅 메시지 불러오는지 확인")
    @WithAccount(TEST_EMAIL)
    @Test
    public void getChatList() throws Exception {
        accountFactory.createAccount("테스트계정2", TEST_EMAIL2);
        accountFactory.verifyEmail(TEST_EMAIL);
        accountFactory.verifyEmail(TEST_EMAIL2);
        Account me = accountRepository.findByEmail(TEST_EMAIL);
        Account partner = accountRepository.findByEmail(TEST_EMAIL2);

        garmentFactory.createGarment(1, partner,
                CITY_PROVINCE, CITY_COUNTRY_DISTRICT, TOWN_TOWNSHIP_NEIGHBORHOOD);
        Garment garment = garmentRepository.findByTitle("제목(1)").get(0);

        // 채팅방 개설
        ChatPartnerForm chatPartnerForm = new ChatPartnerForm();
        chatPartnerForm.setBuyerOrSeller("seller");
        chatPartnerForm.setNickname(partner.getNickname());

        String responseString = mockMvc.perform(post("/garment/" + garment.getId() + "/chatList")
                    .characterEncoding("utf8")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(chatPartnerForm))
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json; charset=utf-8"))
                .andExpect(authenticated().withUsername(TEST_EMAIL))
                .andReturn().getResponse().getContentAsString();

        ChatRoom chatRoom = chatRoomRepository.findBySellerAndBuyerAndGarment(partner, me, garment);
        assertNotNull(chatRoom);
        assertEquals(responseString, "[]");

        // 채팅 보내기
        ChatRoomForm chatRoomForm = new ChatRoomForm();
        chatRoomForm.setSellerNickname(partner.getNickname());
        chatRoomForm.setBuyerNickname(me.getNickname());
        chatRoomForm.setMessage("안녕하세요~!");

        mockMvc.perform(post("/garment/" + garment.getId() + "/chat")
                    .characterEncoding("utf8")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(chatRoomForm))
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json; charset=utf-8"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        // 채팅 메시지 불러오기
        String responseString2 = mockMvc.perform(post("/garment/" + garment.getId() + "/chatList")
                    .characterEncoding("utf8")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(chatPartnerForm))
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json; charset=utf-8"))
                .andExpect(authenticated().withUsername(TEST_EMAIL))
                .andReturn().getResponse().getContentAsString();

        List<MessageForm> messageFormList = objectMapper.readValue(responseString2,
                objectMapper.getTypeFactory().constructCollectionType(List.class, MessageForm.class));
        assertEquals(messageFormList.get(0).getNickname(), me.getNickname());
        assertEquals(messageFormList.get(0).getMessage(), "안녕하세요~!");
        assertEquals(messageFormList.get(0).isMe(), true);
        assertEquals(chatRoom.getChatList().get(0).getMessage(), "안녕하세요~!");
    }


}