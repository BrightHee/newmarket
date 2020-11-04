package com.newmarket.account;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class Account {

    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false)
    private String password;

    private boolean passwordVerified;  // 새 비밀번호 설정 전에 비밀번호 확인용

    private boolean emailVerified;

    private String certificationToken;

    private LocalDateTime certificationTokenGeneratedLocalDateTime;

    private LocalDateTime joinedDateTime;

    private String greetings;

    @Lob
    private String profileImage;

    // 판매자가 구매 신청을 받으면 알림으로 받을 것인지 결정
    @Builder.Default
    private boolean purchaseRegisteredByWeb = true;

    private boolean purchaseRegisteredByEmail;

    // 구매자가 구매 신청 결과를 받으면 알림으로 받을 것인지 결정
    @Builder.Default
    private boolean purchaseResultByWeb = true;

    private boolean purchaseResultByEmail;

    public void createCertificationToken() {
        this.certificationToken = UUID.randomUUID().toString();
        this.certificationTokenGeneratedLocalDateTime = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.certificationToken.equals(token)
                && this.certificationTokenGeneratedLocalDateTime.plusSeconds(30).isAfter(LocalDateTime.now());
    }

    public void finishSignUp() {
        this.emailVerified = true;
        this.joinedDateTime = LocalDateTime.now();
    }

    public void getNewPassword() {
        this.password = UUID.randomUUID().toString().replace("-", "");
    }
}
