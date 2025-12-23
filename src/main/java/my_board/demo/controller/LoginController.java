package my_board.demo.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import my_board.demo.domain.Member;
import my_board.demo.dto.TokenInfo;
import my_board.demo.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor // MemberService 주입
public class LoginController {
    private final MemberService memberService;

    /**
     * 로그인 폼 페이지
     */
    @GetMapping("/login")
    public String loginForm() {
        return "login/loginForm"; // /templates/loginForm.html
    }

    /**
     *  수동 로그인 처리
     */
    @PostMapping("/login")
    public String login(@ModelAttribute Member loginRequest, HttpServletResponse response) {
        // 1. MemberService의 login 메서드 호출 (ID/PW 검증 및 토큰 발급)
        // 토큰 정보(TokenInfo)를 반환받음
        TokenInfo tokenInfo = memberService.login(loginRequest.getLoginId(), loginRequest.getPassword());

        if (tokenInfo == null) {
            // 2. 로그인 실패 (간단한게 URL 파라미터로 에러 표시)
            return "redirect:/login?error=true";
        }

        // 3. 로그인 성공 시, 쿠키(Cookie) 생성
        // HttpOnly 쿠키에 Access Token 저장
        Cookie cookie = new Cookie("accessToken", tokenInfo.getAccessToken());
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24); // 1일 유지
        cookie.setHttpOnly(true);       // 자바스크립트 접근 불가 (보안)

        // 4. 응답(Response)에 쿠키 추가
        response.addCookie(cookie);

        // 5. 게시글 목록으로 리다이렉트
        return "redirect:/posts";
    }

    /**
     * 수동 로그아웃 처리
     */
    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        // 1. 쿠키 삭제
        Cookie cookie = new Cookie("accessToken", null);
        cookie.setMaxAge(0); // 유효시간 0으로 설정 -> 삭제됨
        cookie.setPath("/");

        // 2. 응답에 삭제된 쿠키 추가
        response.addCookie(cookie);

        // 3. 게시글 목록으로 리다이렉트
        return "redirect:/posts";
    }
}
