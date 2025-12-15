package my_board.demo.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter // 모든 필드의 Getter 자동 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 자동 생성
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false) // 로그인 ID (중복 불가)
    private String loginId;

    @Setter
    @Column(nullable = false)
    private String password;

    @Setter
    @Column(nullable = false) // 표시될 이름
    private String nickname;

    @Builder
    public Member(String loginId, String password, String nickname) {
        this.loginId = loginId;
        this.password = password;
        this.nickname = nickname;
    }
}
