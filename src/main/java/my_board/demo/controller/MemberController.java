package my_board.demo.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import my_board.demo.domain.Member;
import my_board.demo.dto.LoginRequestDto;
import my_board.demo.dto.TokenInfo;
import my_board.demo.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/members") // URL 공통 부분
public class MemberController {
    private final MemberService memberService;

    // --- 회원가입 기능 ---
    // 1. 회원가입 폼을 보여주는 페이지
    @GetMapping("/join")
    public String addForm() {
        return "members/addMemberForm"; // templates/members/addMemberForm.html
    }

    // 2. 폼 데이터를 받아서 실제 회원가입 처리
    @PostMapping("/join")
    public String save(Member member) { // 폼 데이터가 Member 객체에 자동으로 바인딩됨
        try {
            memberService.join(member);
        } catch (IllegalStateException e) {
            // 중복 ID가 있을 경우 에러 페이지를 보여줌
            System.out.println(e.getMessage());
            return "redirect:/members/join?error=duplicate";
        }

        return "redirect:/posts"; // 회원가입 성공 시 게시글 목록("/posts")으로 리다이렉트
    }

    // --- 로그인 기능 ---
    // 1. 로그인 페이지 보여주기 (GET /members/login)
    @GetMapping("/login")
    public String loginForm() {
        return "login/loginForm";
    }

    // 2. 로그인 처리 및 쿠키 발급 (POST /members/login)
    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequestDto loginRequest, HttpServletResponse response) {
        // 서비스에서 토큰 받아오기
        TokenInfo tokenInfo = memberService.login(loginRequest.getLoginId(), loginRequest.getPassword());

        if (tokenInfo == null) {
            return "redirect:/members/login?error=true"; // 실패 시 다시 로그인 페이지
        }

        // 쿠키 생성 (HttpOnly)
        Cookie cookie = new Cookie("accessToken", tokenInfo.getAccessToken());
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24); // 1일
        cookie.setHttpOnly(true);

        // 응답에 쿠키 추가
        response.addCookie(cookie);

        return "redirect:/posts";
    }
}
