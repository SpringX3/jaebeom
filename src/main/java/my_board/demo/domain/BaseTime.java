package my_board.demo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass // 1. JPA 엔티티들이 이 클래스를 상속할 경우 필드들도 칼럼으로 인식
@EntityListeners(AuditingEntityListener.class) // 2. Auditing 기능을 포함시킴
public abstract class BaseTime {
    @CreatedDate // 3. 엔티티가 생성되어 저장될 때 시간이 자동 저장
    @Column(updatable = false) // 생성 시간은 수정되지 않도록
    private LocalDateTime createdDate;

    @LastModifiedDate // 4. 조회한 엔티티의 값을 변경할 때 시간이 자동 저장
    private LocalDateTime modifiedDate;
}
