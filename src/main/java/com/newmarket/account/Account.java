package com.newmarket.account;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Account {

    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false)
    private String password;

    private boolean emailVerified;

    private String emailCheckToken;

    private LocalDateTime emailCheckTokenGeneratedDateTime;

    private LocalDateTime joinedDateTime;

    private String greetings;

    private String location;

    @Lob
    private String profileImage;

    // 관심 있는 물품이나 설정한 지역의 물품이 등록되면 알림으로 받을 것인지 결정
    private boolean productRegisteredByWeb;

    private boolean productRegisteredByEmail;

    // 판매자가 구매 신청을 받으면 알림으로 받을 것인지 결정
    private boolean purchaseRegisteredByWeb;

    private boolean purchaseRegisteredByEmail;

    // 구매자가 구매 신청 결과를 받으면 알림으로 받을 것인지 결정
    private boolean purchaseResultByWeb;

    private boolean purchaseResultByEmail;

}