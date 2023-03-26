package stock_market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication
public class StockMarketApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(StockMarketApplication.class);
        application.setDefaultProperties(Collections.singletonMap("server.port", "8080"));
        application.run(args);
    }
}
