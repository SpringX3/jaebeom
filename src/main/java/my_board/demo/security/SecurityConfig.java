package my_board.demo.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. REST API 방식이므로 기본 보안 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable) // 기본 로그인 페이지 안 씀

                // 2. 세션을 사용하지 않음 (Stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. 요청별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 로그인, 회원가입, 메인 페이지, 정적 리소스는 누구나 접근 가능
                        .requestMatchers("/", "/members/login", "/members/join", "/css/**", "/images/**", "/js/**", "/favicon.ico").permitAll()
                        // 게시글 목록 보기(GET /posts)도 누구나 접근 가능 (상세 보기도 포함)
                        .requestMatchers("/posts/**").permitAll()
                        // 그 외 요청(글 쓰기, 수정 등)은 인증된 사용자만 가능
                        // (PostController 내부에서 로그인 체크를 한 번 더 하므로 여기선 유연하게 둠)
                        .anyRequest().authenticated()
                )
                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")                          // 로그아웃 주소 (HTML의 href="/logout"과 일치)
                        .logoutSuccessUrl("/posts")                    // 로그아웃 시 이동할 주소
                        .deleteCookies("accessToken") // 로그아웃 시 쿠키 삭제
                )

                // 4. JWT 필터를 먼저 실행 (UsernamePasswordAuthenticationFilter 앞에서)
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 비밀번호 암호화 (BCrypt 등 자동 설정)
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
