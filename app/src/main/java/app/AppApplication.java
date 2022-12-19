package app;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class AppApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppApplication.class);

	@Autowired
	private ServletWebServerApplicationContext webServerAppCtxt;

	@Value("${register-url}")
	private String registerUrl;

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void registerUrl() throws Exception {
		int port = webServerAppCtxt.getWebServer().getPort();
		String ipAddress = InetAddress.getLocalHost().getHostAddress();

		Map<String, String> map = new HashMap<>();
		map.put("url", StringUtils.join("http://", ipAddress, ":", port, "/app"));

		RestTemplate restTemplate = new RestTemplate();

		try {
			ResponseEntity<Void> response = restTemplate.postForEntity(registerUrl, map, Void.class);

			if (!response.getStatusCode().is2xxSuccessful()) {
				throw new Exception("Server registration didn't return 2xx code.");
			}
		} catch(Exception e) {
			LOGGER.error("Failed to register server. Exiting now.", e);
			System.exit(0);
		}
	}

}
