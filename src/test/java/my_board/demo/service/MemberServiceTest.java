package my_board.demo.service;

import my_board.demo.domain.Member;
import my_board.demo.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest // 1. 실제 스프링 부트 애플리케이션을 실행시킵니다.
@Transactional // 2. 테스트가 끝나면 DB를 롤백(Rollback)합니다. (!!매우 중요!!)
public class MemberServiceTest {
    // 3. 테스트할 대상(Service)과 검증에 필요한 (Repository) 객체를 주입받습니다.
    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Test // 4. 이 메서드가 테스트 케이스임을 선언
    void join_completed() {
        // given(주어진 것): 회원가입 폼에 이런 데이터가 들어왔다.
        Member member = Member.builder()
                .loginId("abcd")
                .password("1234")
                .nickname("테스트유저")
                .build();

        // when(실행): 회원가입(join) 서비스를 실행한다.
        Long savedId = memberService.join(member);

        // then(결과): DB에 잘 저장되었는지 확인한다.
        // 1. 반환된 ID가 null이 아니어야 함
        assertThat(savedId).isNotNull();

        // 2. Repository에서 ID로 직접 조회해서,
        Member findMember = memberRepository.findById(savedId).get();

        // 3. 처음에 넣으려고 했던 member의 loginId와
        //    DB에서 꺼내온 findMember의 loginId가 같은지 검증
        assertThat(member.getLoginId()).isEqualTo(findMember.getLoginId());
    }

    @Test
    void join_failed() {
        // given(주어진 것): 똑같은 loginId를 가진 2명의 회원이 있다.
        Member member1 = Member.builder()
                .loginId("copyid")
                .password("1234")
                .nickname("회원1")
                .build();

        Member member2 = Member.builder()
                .loginId("copyid") // <-- member1과 loginId가 중복됨
                .password("5678")
                .nickname("회원2")
                .build();

        // when(실행): 첫 번째 회원은 정상적으로 가입시킨다.
        memberService.join(member1);

        // then(결과): 두 번째 회원을 가입시킬 때 IllegalStateException이 터져야 성공!
        // 5. assertThrows: 이 람다식을 실행할 때,
        //    지정한 예외(IllegalStateException.class)가 발생하는지 검증
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
            memberService.join(member2); // <-- 예외가 터질 것으로 예상되는 코드
        });

        // 6. (선택) 예외 메세지가 우리가 설정한 메세지("이미 존재하는 아이디입니다.")와 같은지도 검증
        assertThat(e.getMessage()).isEqualTo("이미 존재하는 아이디입니다.");
    }
}
