package com.newmarket.chatRoom;

import com.newmarket.account.Account;
import com.newmarket.garment.Garment;
import com.newmarket.garment.form.ChatRoomForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoom findOrCreateNew(Account me, Account partner, String partnerType, Garment garment) {
        Account seller = partnerType.equals("seller") ? partner : me;
        Account buyer = partnerType.equals("seller") ? me : partner;

        ChatRoom chatRoom = chatRoomRepository.findBySellerAndBuyerAndGarment(seller, buyer, garment);
        if (chatRoom == null) {
            if (garment.isClosed() || seller.equals(me)) {  // 잘못된 요청에 대한 처리
                return null;
            }
            ChatRoom newChatRoom = ChatRoom.builder()
                    .seller(seller).buyer(buyer).garment(garment).build();
            chatRoom = chatRoomRepository.save(newChatRoom);
        }
        return chatRoom;
    }

    public Chat addNewChatMessage(ChatRoomForm chatRoomForm, ChatRoom chatRoom, Account account) {
        Chat chat = new Chat();
        chat.setSender(account);
        chat.setMessage(chatRoomForm.getMessage());
        chat.setSentDateTime(LocalDateTime.now());
        chatRoom.addChat(chat);
        return chat;
    }

}
