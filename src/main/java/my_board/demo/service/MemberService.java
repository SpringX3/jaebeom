package my_board.demo.service;

import lombok.RequiredArgsConstructor;
import my_board.demo.domain.Member;
import my_board.demo.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // final 필드 생성자 자동 주입 (Lombok)
public class MemberService {
    private final MemberRepository memberRepository;

    /**
     * 회원가입 처리
     * @return 저장된 회원의 id
     */
    public Long join(Member member) {
        // 1. ID 중복 검사
        validateDuplicateMember(member);

        // 2. 비밀번호 암호화

        // 3. 회원 정보 저장
        memberRepository.save(member);
        return member.getId();
    }

    /**
     * (신규) 로그인 기능
     * @return 로그인 성공 시 Member 객체, 실패 시 null
     */
    @Transactional(readOnly = true) // 조회(Read) 기능
    public Member login(String loginId, String password) {
        // 1. ID로 회원 조회
        Member member = memberRepository.findByLoginId(loginId)
                .orElse(null); // ID가 없으면 null 반환

        if (member == null) {
            return null; // ID 없음
        }

        // 2. 원본 비밀번호와 DB의 비밀번호가 일치하는지 확인
        if (member.getPassword().equals(password)) {
            return member; // 로그인 성공
        } else {
            return null; // 비밀번호 불일치
        }
    }

    // 중복 회원 검증 로직
    private void validateDuplicateMember(Member member) {
        memberRepository.findByLoginId(member.getLoginId())
                .ifPresent(m -> {
                    throw new IllegalStateException("이미 존재하는 아이디입니다.");
                });
    }
}
