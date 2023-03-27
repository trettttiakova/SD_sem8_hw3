package user_account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication
public class UserAccountApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(UserAccountApplication.class);
        application.setDefaultProperties(Collections.singletonMap("server.port", "8081"));
        application.run(args);
    }
}
