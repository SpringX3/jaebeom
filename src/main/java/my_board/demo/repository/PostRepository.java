package my_board.demo.repository;

import my_board.demo.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    // JpaRepository가 save, findById, deletedById 등을 모두 제공

    // N+1 문제 해결 + 페이징을 위한 쿼리
    // 게시글 목록(N)을 조회할 때, 작성자(1) 정보도 한번에 Join해서 가져옴
    @Query(value = "SELECT p FROM Post p LEFT JOIN FETCH p.member", countQuery = "SELECT count(p) FROM Post p")
    Page<Post> findAllWithMember(Pageable pageable);

    @Query("SELECT p FROM Post p " +
           "LEFT JOIN FETCH p.member " + // 1. 작성자(N:1)도 JOIN FETCH
           //"LEFT JOIN FETCH p.comments " + // 2. 댓글(1:N)도 JOIN FETCH
           "WHERE p.id = :id")
    Optional<Post> findByIdWithMember(@Param("id") Long id);
}
