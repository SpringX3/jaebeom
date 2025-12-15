package my_board.demo.dto;

import lombok.Getter;
import my_board.demo.domain.Post;

import java.time.LocalDateTime;

@Getter
public class PostResponseDto {
    // 1. 보여줘도 되는 안전한 필드만 정의
    private Long id;
    private String title;
    private String content;
    private String authorNickname; // Entity를 가공한, View에 최적화된 필드
    private String authorLoginId; // 작성자 본인 확인을 위한 loginId
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    // 2. DTO가 Entity를 입력받는 생성자
    //    Entity를 예쁜 접시에 옮겨담는 과정
    public PostResponseDto(Post entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.content = entity.getContent();

        // 3. (보안) Entity를 직접 노출하는 대신, 닉네임과 loginId를 꺼내서 담습니다.
        if (entity.getMember()  != null) {
            this.authorNickname = entity.getMember().getNickname();
            this.authorLoginId = entity.getMember().getLoginId();
        } else {
            this.authorNickname = "알 수 없는 사용자"; // 예외 처리
            this.authorLoginId = null;
        }

        this.createdDate = entity.getCreatedDate();
        this.modifiedDate = entity.getModifiedDate();
    }
}
