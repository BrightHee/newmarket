package com.newmarket.notification;

import com.newmarket.account.Account;
import com.newmarket.account.AccountRepository;
import com.newmarket.garment.GarmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class NotificationFactory {

    @Autowired NotificationRepository notificationRepository;
    @Autowired AccountRepository accountRepository;

    public List<Notification> createNotification(int num, String email, NotificationType notificationType) throws InterruptedException {
        Account account = accountRepository.findByEmail(email);
        List<Notification> notifications = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            Notification notification = Notification.builder()
                    .message("테스트입니다.")
                    .link("테스트링크")
                    .account(account)
                    .checked(false)
                    .createdLocalDateTime(LocalDateTime.now())
                    .notificationType(notificationType)
                    .build();
            notifications.add(notification);
            notificationRepository.save(notification);
            Thread.sleep(10);  // saveAll로 한번에 저장하는 것 대신에 간격을 두고 저장
        }
        return notifications;
    }

}
