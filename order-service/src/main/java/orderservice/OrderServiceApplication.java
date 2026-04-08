package orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication(scanBasePackages = {"orderservice", "uk.co.ttingle.commonlib"})
public class OrderServiceApplication {
  static void main(String[] args) {
    SpringApplication.run(OrderServiceApplication.class, args);
  }
}
