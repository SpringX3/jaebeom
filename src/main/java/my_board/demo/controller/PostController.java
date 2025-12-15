package my_board.demo.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import my_board.demo.domain.Member;
import my_board.demo.domain.Post;
import my_board.demo.dto.PostResponseDto;
import my_board.demo.dto.PostSaveRequestDto;
import my_board.demo.dto.PostUpdateRequestDto;
import my_board.demo.repository.PostRepository;
import my_board.demo.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;

// import java.util.List;
import java.util.stream.Collectors;

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
    private final PostRepository postRepository; // (조회 기능은 Repository에서 바로 DTO 반환)

    // 게시글 전체 목록 페이지
    @Operation(summary = "게시글 목록 페이지", description = "게시글 목록을 보여주는 HTML 화면을 요청합니다.") // [2] 설명 추가
    @GetMapping
    public String listPosts(Model model, @PageableDefault(page = 0, size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        // Repository에서 Pageable을 사용해 Page<Post>를 가져옴
        Page<Post> postPage = postRepository.findAllWithMember(pageable);

        // Page<Post> -> Page<PostResponseDto> 로 변환 (map 함수 사용)
        Page<PostResponseDto> postDtoPage = postPage.map(PostResponseDto::new);

        // model에 List가 아닌 Page 객체('postPage')를 담아서 전달
        // HTML에서는 'postPage.content'로 게시글 목록에 접근
        model.addAttribute("postPage", postDtoPage);

        return "posts/postList"; // /templates/posts/postList.html
    }

    // --- Create ---
    // 1. 글 작성 폼(HTML)을 보여줌 (GET)
    @Operation(summary = "게시글 작성 페이지", description = "게시글 작성 페이지를 보여줍니다.")
    @GetMapping("/add")
    public String addForm(HttpSession session) {
        // 로그인하지 않은 사용자는 로그인 페이지로 리다이렉트
        if (session.getAttribute("loginMember") == null) {
            return "redirect:/login";
        }
        return "posts/postForm"; // /templates/posts/postForm.html
    }

    // 2. 폼 데이터를 받아서 실제 글 작성 처리 (POST)
    @Operation(summary = "게시글 작성 기능", description = "Member 정보를 가져와 게시글을 작성합니다.")
    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String savePost(@ModelAttribute PostSaveRequestDto Save_Req, HttpSession session) {
        // 세션에서 로그인한 Member 객체 가져오기
        Member loginMember = (Member) session.getAttribute("loginMember");

        // 세션에 Member가 없으면(로그인 안 했으면) 로그인 페이지로
        if (loginMember == null) {
            return "redirect:/login";
        }
        // 세션에서 loginId 가져오기
        String loginId = loginMember.getLoginId();

        try {
            postService.save(Save_Req, loginId);
            // DB 오류(NULL 저장 시도 등)를 포함한 모든 예외(Exception)를 잡도록 변경
        } catch (Exception e) {
            System.out.println("Error saving post: " + e.getMessage());
            // 저장 실패 시, 로그인 페이지가 아닌 글 작성 폼으로 다시 리다이렉트
            return "redirect:/posts/add?error=true";
        }

        return "redirect:/posts"; // 저장이 끝나면 상세 페이지로 리다이렉트
    }

    // --- Read ---
    // 3. 글 상세 조회
    @Operation(summary = "게시글 조회", description = "게시글의 제목과 내용 등 상세 내용을 보여줍니다.")
    @GetMapping("/{id}")
    public String postDetail(@PathVariable Long id, Model model) {
        // N+1 문제를 해결하기 위해 Fetch Join을 사용한 findById
        Post post = postRepository.findByIdWithMember(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));

        // Entity -> DTO 변환 (PostReponseDto는 직접 만들어야함. title, content, nickname 등 포함)
        PostResponseDto responseDto = new PostResponseDto(post);
        model.addAttribute("post", responseDto);
        return "posts/postDetail"; // /templates/posts/postDetail.html
    }

    // --- Update ---
    // 4. 글 수정 폼(HTML)을 보여줌 (GET)
    @Operation(summary = "게시글 수정 페이지", description = "게시글 수정 페이지를 보여줍니다.")
    @GetMapping("/{id}/edit")
    public String updateForm(@PathVariable Long id, Model model, HttpSession session) {
        Post post = postRepository.findByIdWithMember(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));

        // 작성자 본인 확인
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null || post.getMember() == null || !post.getMember().getLoginId().equals(loginMember.getLoginId())) {
            // 로그인하지 않았거나, 작성자가 아니면 목록으로 리다이렉트
            return "redirect:/posts";
        }

        PostResponseDto responseDto = new PostResponseDto(post);
        model.addAttribute("post", responseDto);

        return "posts/postEditForm"; // /templates/posts/postEditForm.html
    }

    // 5. 폼 데이터를 받아서 실제 글 수정 처리 (POST)
    @Operation(summary = "게시글 수정", description = "작성자 확인 후 게시글을 수정합니다.")
    @PostMapping(value = "/{id}/edit",  consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String updatePost(@PathVariable Long id, @ModelAttribute PostUpdateRequestDto Update_Req, HttpSession session) {
        // 작성자 본인 확인
        Post post = postRepository.findByIdWithMember(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null || post.getMember() == null || !post.getMember().getLoginId().equals(loginMember.getLoginId())) {
            return "redirect:/posts"; // 작성자가 아니면 수정 불가
        }

        postService.update(id, Update_Req);
        return "redirect:/posts/" + id; // 수정이 끝나면 상세 페이지로 리다이렉트
    }

    // --- Delete ---
    // 6. 글 삭제 처리 (POST, 간단하게)
    @Operation(summary = "게시글 삭제", description = "작성자 확인 후 게시글을 삭제합니다.")
    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable Long id, HttpSession session) {
        // 작성자 본인 확인
        Post post = postRepository.findByIdWithMember(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null || post.getMember() == null || !post.getMember().getLoginId().equals(loginMember.getLoginId())) {
            return "redirect:/posts"; // 작성자가 아니면 삭제 불가
        }

        postService.delete(id);
        return "redirect:/posts"; // 삭제가 끝나면 목록 페이지로 리다이렉트
    }
}
