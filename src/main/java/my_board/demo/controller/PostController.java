package my_board.demo.controller;

import lombok.RequiredArgsConstructor;
import my_board.demo.domain.Member;
import my_board.demo.domain.Post;
import my_board.demo.dto.PostResponseDto;
import my_board.demo.dto.PostSaveRequestDto;
import my_board.demo.dto.PostUpdateRequestDto;
import my_board.demo.repository.MemberRepository;
import my_board.demo.repository.PostRepository;
import my_board.demo.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;

@Tag(name = "게시판 기능", description = "게시글 조회/작성/수정/삭제") // [1] 그룹 이름 추가
@Controller
@RequiredArgsConstructor
@RequestMapping("/posts") // 공통 URL: /posts
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (작성자 아님)"),
        @ApiResponse(responseCode = "404", description = "리소스 없음 (잘못된 ID)"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
})
public class PostController {
    private final PostService postService;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository; // 현재 사용자 조회용

    // 현재 로그인한 사용자를 가져오는 헬퍼 메서드
    private Member getLoginMember() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        // 인증된 사용자의 ID(loginId)로 DB에서 조회
        return memberRepository.findByLoginId(auth.getName()).orElse(null);
    }

    // 게시글 전체 목록 페이지
    @Operation(summary = "게시글 목록 페이지", description = "게시글 목록을 보여주는 HTML 화면을 요청합니다.")
    @GetMapping
    public String listPosts(Model model, @PageableDefault(page = 0, size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Post> postPage = postRepository.findAllWithMember(pageable);
        Page<PostResponseDto> postDtoPage = postPage.map(PostResponseDto::new);
        model.addAttribute("postPage", postDtoPage);
        model.addAttribute("loginMember", getLoginMember());
        return "posts/postList";
    }

    // --- Create ---
    // 1. 글 작성 폼(HTML)을 보여줌 (GET)
    @Operation(summary = "게시글 작성 페이지", description = "게시글 작성 페이지를 보여줍니다.")
    @GetMapping("/add")
    public String addForm() {
        Member loginMember = getLoginMember();
        if (loginMember == null) {
            return "redirect:/login";
        }
        return "posts/postForm";
    }

    // 2. 폼 데이터를 받아서 실제 글 작성 처리 (POST)
    @Operation(summary = "게시글 작성 기능", description = "Member 정보를 가져와 게시글을 작성합니다.")
    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String savePost(@ModelAttribute PostSaveRequestDto Save_Req) {
        Member loginMember = getLoginMember();

        if (loginMember == null) {
            return "redirect:/login";
        }
        String loginId = loginMember.getLoginId();

        try {
            postService.save(Save_Req, loginId);
        } catch (Exception e) {
            System.out.println("Error saving post: " + e.getMessage());
            return "redirect:/posts/add?error=true";
        }

        return "redirect:/posts";
    }

    // --- Read ---
    // 3. 글 상세 조회
    @Operation(summary = "게시글 조회", description = "게시글의 제목과 내용 등 상세 내용을 보여줍니다.")
    @GetMapping("/{id}")
    public String postDetail(@PathVariable Long id, Model model) {
        // N+1 문제를 해결하기 위해 Fetch Join을 사용한 findById
        Post post = postRepository.findByIdWithMember(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));

        PostResponseDto responseDto = new PostResponseDto(post);
        model.addAttribute("post", responseDto);

        return "posts/postDetail";
    }

    // --- Update ---
    // 4. 글 수정 폼(HTML)을 보여줌 (GET)
    @Operation(summary = "게시글 수정 페이지", description = "게시글 수정 페이지를 보여줍니다.")
    @GetMapping("/{id}/edit")
    public String updateForm(@PathVariable Long id, Model model) {
        Post post = postRepository.findByIdWithMember(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));

        // 작성자 본인 확인
        Member loginMember = getLoginMember();
        if (loginMember == null || post.getMember() == null || !post.getMember().getLoginId().equals(loginMember.getLoginId())) {
            return "redirect:/posts";
        }

        PostResponseDto responseDto = new PostResponseDto(post);
        model.addAttribute("post", responseDto);

        return "posts/postEditForm";
    }

    // 5. 폼 데이터를 받아서 실제 글 수정 처리 (POST)
    @Operation(summary = "게시글 수정", description = "작성자 확인 후 게시글을 수정합니다.")
    @PostMapping(value = "/{id}/edit",  consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String updatePost(@PathVariable Long id, @ModelAttribute PostUpdateRequestDto Update_Req) {
        // 작성자 본인 확인
        Post post = postRepository.findByIdWithMember(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));

        // 작성자 본인 확인
        Member loginMember = getLoginMember();
        if (loginMember == null || post.getMember() == null || !post.getMember().getLoginId().equals(loginMember.getLoginId())) {
            return "redirect:/posts";
        }

        postService.update(id, Update_Req);
        return "redirect:/posts/" + id;
    }

    // --- Delete ---
    // 6. 글 삭제 처리 (POST, 간단하게)
    @Operation(summary = "게시글 삭제", description = "작성자 확인 후 게시글을 삭제합니다.")
    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable Long id) {
        // 작성자 본인 확인
        Post post = postRepository.findByIdWithMember(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));

        // 작성자 본인 확인
        Member loginMember = getLoginMember();
        if (loginMember == null || post.getMember() == null || !post.getMember().getLoginId().equals(loginMember.getLoginId())) {
            return "redirect:/posts";
        }

        postService.delete(id);
        return "redirect:/posts";
    }
}
