package com.newmarket.modules.chatRoom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newmarket.infra.MockMvcTest;
import com.newmarket.modules.account.Account;
import com.newmarket.modules.account.AccountFactory;
import com.newmarket.modules.account.AccountRepository;
import com.newmarket.modules.account.WithAccount;
import com.newmarket.modules.chatRoom.form.MessageForm;
import com.newmarket.modules.garment.Garment;
import com.newmarket.modules.garment.GarmentFactory;
import com.newmarket.modules.garment.GarmentRepository;
import com.newmarket.modules.garment.form.ChatPartnerForm;
import com.newmarket.modules.garment.form.ChatRoomForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTest
class ChatRoomControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private GarmentFactory garmentFactory;
    @Autowired private GarmentRepository garmentRepository;
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

        mockMvc.perform(post("/garment/" + garment2.getId() + "/chat")
                .characterEncoding("utf8")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chatRoomForm2))
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(authenticated().withUsername(TEST_EMAIL));
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
