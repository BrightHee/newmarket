package com.newmarket.notification;

import com.newmarket.account.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void markAsRead(List<Notification> notifications) {
        notifications.forEach(notification -> notification.setChecked(true));
    }

    public void deleteNotifications(Account account, boolean checked) {
        notificationRepository.deleteByAccountAndChecked(account, checked);
    }
}
