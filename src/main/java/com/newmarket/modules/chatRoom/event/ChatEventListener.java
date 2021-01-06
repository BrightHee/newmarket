package com.newmarket.modules.chatRoom.event;

import com.newmarket.modules.account.Account;
import com.newmarket.modules.chatRoom.Chat;
import com.newmarket.modules.chatRoom.ChatRoom;
import com.newmarket.modules.notification.Notification;
import com.newmarket.modules.notification.NotificationRepository;
import com.newmarket.modules.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
