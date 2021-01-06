package com.newmarket.modules.notification;

import com.newmarket.infra.MockMvcTest;
import com.newmarket.modules.account.WithAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@MockMvcTest
class NotificationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired NotificationFactory notificationFactory;

    private final String TEST_EMAIL = "test@email.com";

    @DisplayName("알림 리스트 화면 보이는지 확인")
    @WithAccount(TEST_EMAIL)
    @Test
    public void getNotifications() throws Exception {
        List<Notification> notifications = notificationFactory.createNotification(3, TEST_EMAIL,
                NotificationType.SENT_BUYER_CHAT_MESSAGES);
        mockMvc.perform(get("/notifications"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("notification/list"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
        notifications.forEach(notification -> {
            assertTrue(notification.isChecked());
        });
    }

}
