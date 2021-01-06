package com.newmarket.modules.garment;

import com.newmarket.modules.account.Account;
import com.newmarket.modules.area.Area;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class Garment {

    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String content;

    @Lob
    private String image;

    @Enumerated(EnumType.STRING)
    private GarmentType type;

    private Integer price;

    @ManyToOne
    private Account account;

    @ManyToOne
    private Area area;

    private LocalDateTime updatedDateTime;

    private boolean closed;

    private LocalDateTime closedDateTime;

    @Builder.Default
    @ManyToMany
    private Set<Account> chatRoomPartners = new HashSet<>();

    public boolean checkIfValidAccessToModify(Account account) {
        return !this.closed && this.account.equals(account) && account.isEmailVerified();
    }

    public void addChatRoomPartner(Account account) {
        this.chatRoomPartners.add(account);
    }

}
