package com.newmarket.modules.chatRoom;

import com.newmarket.modules.account.Account;
import com.newmarket.modules.garment.Garment;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class ChatRoom {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Account seller;

    @ManyToOne
    private Account buyer;

    @ManyToOne
    private Garment garment;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Chat> chatList = new ArrayList<>();

    public boolean hasMemberOf(Account account) {
        return this.seller.equals(account) || this.buyer.equals(account);
    }

    public void addChat(Chat chat) {
        this.chatList.add(chat);
        chat.setChatRoom(this);
    }
}
