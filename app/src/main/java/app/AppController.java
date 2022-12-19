package app;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class AppController {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppController.class);
  private final AtomicLong sleepDuration = new AtomicLong(0);

  @PostMapping(value = "/send")
  public JsonNode send(@RequestBody JsonNode requestBody) throws Exception {
    Thread.sleep(sleepDuration.get());
    return requestBody;
  }

  @GetMapping(value = "/health")
  public Map<String, String> doHealthCheck() {
    return Collections.singletonMap("status", "ok");
  }

  @PostMapping(value = "/infect")
  public void infect(HttpServletResponse response) {
    LOGGER.info("Infected app instance.");
    sleepDuration.set(10000);
    response.setStatus(200);
  }

  @PostMapping(value = "/heal")
  public void heal(HttpServletResponse response) {
    LOGGER.info("Healed app instance.");
    sleepDuration.set(0);
    response.setStatus(200);
  }

}
