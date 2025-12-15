package my_board.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import my_board.demo.domain.Member;
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
    public String login(@ModelAttribute Member loginRequest, HttpServletRequest request) {
        // 1. MemberService의 login 메서드 호출 (ID/PW 검증)
        Member loginMember = memberService.login(loginRequest.getLoginId(), loginRequest.getPassword());

        if (loginMember == null) {
            // 2. 로그인 실패 (간단한게 URL 파라미터로 에러 표시)
            return "redirect:/login?error=true";
        }

        // 3. 로그인 성공 시, 세션(Session) 생성
        HttpSession session = request.getSession();
        // 4. 세션에 로그인한 회원 정보(Member 객체) 저장
        session.setAttribute("loginMember", loginMember);

        // 5. 게시글 목록으로 리다이렉트
        return "redirect:/posts";
    }

    /**
     * 수동 로그아웃 처리
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        // 1. 세션 가져오기 (없으면 새로 만들지 않음)
        HttpSession session = request.getSession(false);
        if (session != null) {
            // 2. 세션 무효화
            session.invalidate();
        }
        // 3. 게시글 목록으로 리다이렉트
        return "redirect:/posts";
    }
}
