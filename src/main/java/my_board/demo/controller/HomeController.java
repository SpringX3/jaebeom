package my_board.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home() {
        // 메인 페이지(/)로 오면 게시글 목록(/posts)으로 리다이렉트
        return "redirect:/posts";
    }
}
