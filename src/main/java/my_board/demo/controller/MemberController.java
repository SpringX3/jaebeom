package my_board.demo.controller;

import lombok.RequiredArgsConstructor;
import my_board.demo.domain.Member;
import my_board.demo.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/members") // URL 공통 부분
public class MemberController {
    private final MemberService memberService;

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
            // [실제로는] 중복 ID가 있을 경우 에러 페이지를 보여줘야 함
            System.out.println(e.getMessage());
            return "redirect:/members/join"; // 간단하게 다시 폼으로 리다이렉트
        }

        return "redirect:/posts"; // 회원가입 성공 시 게시글 목록("/posts")으로 리다이렉트
    }
}
