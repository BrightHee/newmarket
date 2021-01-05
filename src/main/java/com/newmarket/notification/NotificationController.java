package com.newmarket.notification;

import com.newmarket.account.Account;
import com.newmarket.account.annotation.AuthenticatedAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @ModelAttribute("account")
    public Account account(@AuthenticatedAccount Account account) {
        return account;
    }

    @GetMapping("/notifications")
    public String getNotifications(@AuthenticatedAccount Account account, Model model) {
        List<Notification> notifications = notificationRepository.findByAccountAndCheckedOrderByCreatedLocalDateTimeDesc(account, false);
        long numberOfChecked = notificationRepository.countByAccountAndChecked(account, true);
        model.addAttribute("notifications", notifications);
        model.addAttribute("numberOfChecked", numberOfChecked);
        model.addAttribute("numberOfNotChecked", notifications.size());
        model.addAttribute("isRead", false);
        putClassifiedNotificationsIntoModel(model, notifications);
        notificationService.markAsRead(notifications);
        return "notification/list";
    }

    @GetMapping("/notifications/old")
    public String getOldNotifications(@AuthenticatedAccount Account account, Model model) {
        List<Notification> notifications = notificationRepository.findByAccountAndCheckedOrderByCreatedLocalDateTimeDesc(account, true);
        long numberOfNotChecked = notificationRepository.countByAccountAndChecked(account, false);
        model.addAttribute("notifications", notifications);
        model.addAttribute("numberOfChecked", notifications.size());
        model.addAttribute("numberOfNotChecked", numberOfNotChecked);
        model.addAttribute("isRead", true);
        putClassifiedNotificationsIntoModel(model, notifications);
        return "notification/list";
    }

    @PostMapping("/notifications/delete")
    public String deleteOldNotifications(@AuthenticatedAccount Account account) {
        notificationService.deleteNotifications(account, true);
        return "redirect:/notifications";
    }

    private void putClassifiedNotificationsIntoModel(Model model, List<Notification> notifications) {
        List<Notification> sellerChatNotifications = new ArrayList<>();
        List<Notification> buyerChatNotifications = new ArrayList<>();
        notifications.forEach(notification -> {
            switch (notification.getNotificationType()) {
                case SENT_SELLER_CHAT_MESSAGES: sellerChatNotifications.add(notification); break;
                case SENT_BUYER_CHAT_MESSAGES: buyerChatNotifications.add(notification); break;
            }
        });
        model.addAttribute("sellerChatNotifications", sellerChatNotifications);
        model.addAttribute("buyerChatNotifications", buyerChatNotifications);
    }

}
