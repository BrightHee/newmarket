package com.newmarket.modules.chatRoom.event;

import com.newmarket.modules.chatRoom.Chat;
import com.newmarket.modules.chatRoom.ChatRoom;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SellerChatSendEvent {

    private final Chat chat;

    private final ChatRoom chatRoom;

}
