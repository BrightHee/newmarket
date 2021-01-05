package com.newmarket.chatRoom.event;

import com.newmarket.chatRoom.Chat;
import com.newmarket.chatRoom.ChatRoom;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BuyerChatSendEvent {

    private final Chat chat;

    private final ChatRoom chatRoom;

}
