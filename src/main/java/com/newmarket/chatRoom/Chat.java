package com.newmarket.chatRoom;

import com.newmarket.account.Account;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
public class Chat {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Account sender;

    @Lob
    private String message;

    private LocalDateTime sentDateTime;

    @ManyToOne
    private ChatRoom chatRoom;

}
