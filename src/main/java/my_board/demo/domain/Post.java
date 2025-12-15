package my_board.demo.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Post extends BaseTime { // 1. BaseTime 상속 (createdDate, modifiedDate 자동 포함)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // 제목

    @Column(nullable = false, columnDefinition = "TEXT") // 2. 내용을 길게 쓸 수 있도록 TEXT 타입
    private String content; // 본문 내용

    // 3. 연관관계: '작성자'
    @ManyToOne(fetch = FetchType.LAZY) // N:1 관계. Post(N) : Member(1)
    @JoinColumn(name = "member_id") // DB에는 'member_id'라는 FK(외래키) 칼럼으로 생성됨
    private Member member; // '작성자'를 String이 아닌 Member 객체로 관리

    // 4. Builder (Member에서 배운 내용)
    @Builder
    public Post(String title, String content, Member member) {
        this.title = title;
        this.content = content;
        this.member = member;
    }

    // 5. 수정(Update)을 위한 비즈니스 로직
    //    서비스(Service) 계층에서 엔티티의 데이터를 직접 수정하는 대신,
    //    엔티티가 스스로 상태를 변경하도록 메서드를 제공 (객체지향적)
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        // save()가 호출되면 @LastModifiedDate에 의해 modifiedDate는 자동으로 업데이트됨
    }
}
