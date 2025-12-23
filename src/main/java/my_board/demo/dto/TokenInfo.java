package my_board.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class TokenInfo {
    private String grantType;    // JWT 권한 타입 (Bearer)
    private String accessToken;  // 액세스 토큰
    private String refreshToken; // 리프레시 토큰
}
