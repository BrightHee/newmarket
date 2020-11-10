package com.newmarket.garment;

import com.newmarket.account.Account;
import com.newmarket.area.Area;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

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

}
