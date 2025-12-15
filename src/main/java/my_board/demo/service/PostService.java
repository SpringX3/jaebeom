package my_board.demo.service;

import lombok.RequiredArgsConstructor;
import my_board.demo.domain.Member;
import my_board.demo.domain.Post;
import my_board.demo.dto.PostSaveRequestDto;
import my_board.demo.dto.PostUpdateRequestDto;
import my_board.demo.repository.MemberRepository;
import my_board.demo.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 1. 중요!

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository; // 작성자(Member)를  찾기 위해 필요

    /**
     * 게시글 생성(Create)
     */
    @Transactional
    public Long save(PostSaveRequestDto Save_Req, String loginId) {
        // 2. DTO로부터 작성자(Member)를 찾아서 주입
        //    (지금은 로그인 기능이 없으므로, 임시로 loginId를 받아서 처리
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이디의 유저가 없습니다. id=" + loginId));

        Post post = Save_Req.toEntity(member); // DTO -> Entity 변환 (Member 주입)
        postRepository.save(post);

        return post.getId();
    }

    /**
     * 게시글 수정(Update)
     */
    @Transactional // 3. JPA의 '더티 체킹(Dirty Checking)'을 위해 필수!
    public Long update(Long id, PostUpdateRequestDto Update_Req) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));

        // 4. Post 엔티티에 만들어둔 update 메서드 호출
        post.update(Update_Req.getTitle(), Update_Req.getContent());

        // 5. @Transactional 덕분에,
        //    'postRepository.save(post)'를 호출하지 않아도
        //    JPA가 변경된 내용을 감지(Dirty Checking)하고 DB에 자동으로 UPDATE 쿼리를 날림
        //    (BaseTime의 modifiedDate도 이때 자동으로 업데이트됨)

        return id;
    }

    /**
     * 게시글 삭제(Delete)
     */
    @Transactional
    public void delete(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));

        postRepository.delete(post); // delete() 메서드를 직접 호출 (deleteById도 가능)
    }

    // (참고) 게시글 조회(Read) 기능은 Controller에서 바로 DTO로 변환하여 반환하는 것이 좋음
    //       (Service에 만들어도 무방)
}
