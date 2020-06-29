package stunts.wojbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@EnableScheduling
@EntityScan("stunts.wojbot.entity")
@EnableJpaRepositories("stunts.wojbot.repository")
@SpringBootApplication
public class WojBot {

	public static void main(String[] args) {
		SpringApplication.run(WojBot.class, args);
	}

	@Bean
	public RestTemplate getRestClient() {
		return new RestTemplate();
	}

}
