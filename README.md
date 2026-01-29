# 📋 Spring Boot Board Project (with Monitoring)

이 프로젝트는 **Spring Boot 3**를 기반으로 개발된 게시판 서비스입니다.  
사용자 인증(JWT), 게시글 CRUD 기능을 포함하며, **Prometheus와 Grafana를 활용한 서버 모니터링 환경**까지 구축한 풀스택/DevOps 실습 프로젝트입니다.

## 🛠 Tech Stack

### Backend
* **Java 21**
* **Spring Boot 3.5.7**
* **Spring Data JPA** (PostgreSQL)
* **Spring Security** + **JWT**
* **QueryDSL** (또는 JPQL/Fetch Join 사용)

### Frontend
* **Thymeleaf** (Server Side Rendering)
* HTML/CSS (Basic Layout)

### DevOps & Monitoring
* **Docker & Docker Compose**
* **GitHub Actions** (CI/CD)
* **AWS EC2**
* **Prometheus** (Metrics Collection)
* **Grafana** (Visualization)

---

## 💡 Key Features

### 1. 회원 관리 (Member)
* **회원가입**: 로그인 ID, 비밀번호, 닉네임을 통한 가입 (중복 ID 체크).
* **로그인**: JWT(Access Token) 발급 및 **HttpOnly Cookie** 저장 방식으로 보안 강화.
* **인증/인가**: Spring Security Filter Chain을 통해 보호된 리소스 접근 제어.

### 2. 게시판 (Board)
* **CRUD**: 게시글 작성, 조회, 수정, 삭제.
* **페이징(Pagination)**: Spring Data JPA `Pageable`을 이용한 게시글 목록 페이징 처리.
* **권한 확인**: 작성자 본인만 수정/삭제 가능하도록 서버 측 검증 로직 구현.
* **성능 최적화**: `Fetch Join`을 사용하여 게시글 조회 시 N+1 문제 해결.

### 3. 모니터링 (Monitoring)
* **Prometheus**: 15초 간격으로 애플리케이션의 메트릭(CPU, 메모리, HTTP 요청 등) 수집.
* **Grafana**: 수집된 데이터를 시각화하여 대시보드로 제공.

---

## 📂 Architecture & Directory

```text
src/main/java/my_board/demo
├── controller    # Web Layer (요청 처리)
├── service       # Business Layer (비즈니스 로직)
├── repository    # Data Access Layer (DB 접근)
├── domain        # Entity (DB 테이블 매핑)
├── dto           # Data Transfer Object (계층 간 데이터 전송)
└── security      # JWT 및 Security 설정