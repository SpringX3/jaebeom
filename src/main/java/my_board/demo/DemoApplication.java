package my_board.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing // (!!) JPA Auditing 기능을 켭니다.
@SpringBootApplication
public class DemoApplication {
	public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
	}
}
