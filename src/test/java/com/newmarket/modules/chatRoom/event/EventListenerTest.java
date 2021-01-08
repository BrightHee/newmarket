package com.newmarket.modules.chatRoom.event;

import com.newmarket.infra.ContainerBaseTest;
import com.newmarket.infra.MockMvcTest;
import com.newmarket.modules.account.Account;
import com.newmarket.modules.account.AccountFactory;
import com.newmarket.modules.account.AccountRepository;
import com.newmarket.modules.account.WithAccount;
import com.newmarket.modules.chatRoom.ChatRoom;
import com.newmarket.modules.chatRoom.ChatRoomRepository;
import com.newmarket.modules.chatRoom.ChatRoomService;
import com.newmarket.modules.garment.Garment;
import com.newmarket.modules.garment.GarmentFactory;
import com.newmarket.modules.garment.GarmentRepository;
import com.newmarket.modules.garment.form.ChatRoomForm;
import com.newmarket.modules.notification.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@MockMvcTest
class EventListenerTest extends ContainerBaseTest {

    @Autowired private AccountFactory accountFactory;
    @Autowired private GarmentFactory garmentFactory;
    @Autowired private AccountRepository accountRepository;
    @Autowired private GarmentRepository garmentRepository;
    @Autowired private ChatRoomService chatRoomService;
    @Autowired private ChatRoomRepository chatRoomRepository;
    @Autowired private NotificationRepository notificationRepository;
    @MockBean private AsyncConfigurer asyncConfigurer;

    private final String TEST_EMAIL = "test@email.com";
    private final String TEST_EMAIL2 = "test2@email.com";
    private final String CITY_PROVINCE = "서울특별시";
    private final String CITY_COUNTRY_DISTRICT = "광진구";
    private final String TOWN_TOWNSHIP_NEIGHBORHOOD = "화양동";

    @BeforeEach
    public void beforeEach() {
        when(asyncConfigurer.getAsyncExecutor()).thenReturn(new SyncTaskExecutor());
    }

    @DisplayName("채팅방에서 채팅을 보내면 반대쪽 사람에게 알림이 가는지 확인")
    @WithAccount(TEST_EMAIL)
    @Test
    public void SellerChatSendEvent() throws Exception {
        // 구매자(나)가 판매자(상대)에게 채팅 보낼 때 상대에게 알림오는지 확인
        accountFactory.createAccount("테스트계정2", TEST_EMAIL2);
        accountFactory.verifyEmail(TEST_EMAIL);
        accountFactory.verifyEmail(TEST_EMAIL2);
        Account me = accountRepository.findByEmail(TEST_EMAIL);
        Account partner = accountRepository.findByEmail(TEST_EMAIL2);

        garmentFactory.createGarment(1, partner,
                CITY_PROVINCE, CITY_COUNTRY_DISTRICT, TOWN_TOWNSHIP_NEIGHBORHOOD);
        Garment garment = garmentRepository.findByTitle("제목(1)").get(0);

        chatRoomService.findOrCreateNew(me, partner, "seller", garment);
        ChatRoomForm chatRoomForm = new ChatRoomForm();
        chatRoomForm.setSellerNickname(partner.getNickname());
        chatRoomForm.setBuyerNickname(me.getNickname());
        chatRoomForm.setMessage("안녕하세요~!");

        ChatRoom chatRoom = chatRoomRepository.findBySellerAndBuyerAndGarment(partner, me, garment);
        chatRoomService.addNewChatMessage(chatRoomForm, chatRoom, me);

        assertEquals(notificationRepository.countByAccountAndChecked(partner, false), 1);

        // 판매자(상대)가 구매자(나)에게 채팅 보낼 때 나에게 알림오는지 확인
        chatRoomForm.setMessage("네ㅎㅎ");
        chatRoomService.addNewChatMessage(chatRoomForm, chatRoom, partner);

        assertEquals(notificationRepository.countByAccountAndChecked(me, false), 1);
    }

}
