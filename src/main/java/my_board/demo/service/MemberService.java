package my_board.demo.service;

import lombok.RequiredArgsConstructor;
import my_board.demo.domain.Member;
import my_board.demo.dto.TokenInfo;
import my_board.demo.repository.MemberRepository;
import my_board.demo.security.JwtTokenProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // final 필드 생성자 자동 주입 (Lombok)
public class MemberService {
    private final MemberRepository memberRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 처리
     * @return 저장된 회원의 id
     */
    public Long join(Member member) {
        // 1. ID 중복 검사
        validateDuplicateMember(member);

        // 2. 비밀번호 암호화
        member.setPassword(passwordEncoder.encode(member.getPassword()));

        // 3. 회원 정보 저장
        memberRepository.save(member);
        return member.getId();
    }

    /**
     * JWT 토큰 반환
     */
    @Transactional
    public TokenInfo login(String loginId, String password) {
        // 1. Login ID/PW를 기반으로 Authentication 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginId, password);

        try {
            // 2. 실제 검증 (사용자 비밀번호 체크)
            // authenticate() 실행 시 CustomUserDetailsService.loadUserByUsername 실행됨
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

            // 3. 인증 정보를 기반으로 JWT 토큰 생성
            return jwtTokenProvider.generateToken(authentication);
        } catch (Exception e) {
            // 로그인 실패 시 null 반환 (Controller에서 처리)
            return null;
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
