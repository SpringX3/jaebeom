package my_board.demo.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import my_board.demo.domain.Member;
import my_board.demo.domain.Post;

@Getter
@Setter
@NoArgsConstructor
public class PostSaveRequestDto {
    private String title;
    private String content;
    // (참고) 작성자(Member)는 DTO로 받지 않음
    //       로그인한 사용자의 정보를 Service에서 직접 가져와 주입

    @Builder
    public PostSaveRequestDto(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // DTO를 Entity로 변환하는 메서드 (Service에서 사용)
    public Post toEntity(Member member) {
        return Post.builder()
                .title(title)
                .content(content)
                .member(member) // 작성자(Member) 객체를 주입
                .build();
    }
}
