package com.newmarket.chatRoom.event;

import com.newmarket.account.Account;
import com.newmarket.chatRoom.Chat;
import com.newmarket.chatRoom.ChatRoom;
import com.newmarket.notification.Notification;
import com.newmarket.notification.NotificationRepository;
import com.newmarket.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Async
@Component
@Transactional
@RequiredArgsConstructor
public class ChatEventListener {

    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleSellerChatSendEvent(SellerChatSendEvent sellerChatSendEvent) {
        Account buyer = sellerChatSendEvent.getChatRoom().getBuyer();
        if (buyer.isSentSellerChatMessages()) {
            createNotification(buyer, sellerChatSendEvent.getChat(), sellerChatSendEvent.getChatRoom(), NotificationType.SENT_SELLER_CHAT_MESSAGES);
        }
    }

    @EventListener
    public void handleBuyerChatSendEvent(BuyerChatSendEvent buyerChatSendEvent) {
        Account seller = buyerChatSendEvent.getChatRoom().getSeller();
        if (seller.isSentBuyerChatMessages()) {
            createNotification(seller, buyerChatSendEvent.getChat(), buyerChatSendEvent.getChatRoom(), NotificationType.SENT_BUYER_CHAT_MESSAGES);
        }
    }

    private void createNotification(Account account, Chat chat, ChatRoom chatRoom, NotificationType type) {
        Notification notification = new Notification();
        notification.setMessage(chat.getMessage());
        notification.setLink("/garment/" + chatRoom.getGarment().getId());
        notification.setChecked(false);
        notification.setAccount(account);
        notification.setCreatedLocalDateTime(LocalDateTime.now());
        notification.setNotificationType(type);
        notificationRepository.save(notification);
    }

}
