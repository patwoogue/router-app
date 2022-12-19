package router;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@RestController
public class RouterController {

  private static final Logger LOGGER = LoggerFactory.getLogger(RouterController.class);

  @Autowired
  AppServerList appServerList;

  @Autowired
  RestTemplate restTemplate;

  @PostMapping(value = "/send")
  @Retryable(
    value = ResourceAccessException.class,
    maxAttempts = 3,
    backoff = @Backoff(delay = 5000)
  )
  public ResponseEntity<JsonNode> send(@RequestBody JsonNode requestBody) throws Exception {
    String appUrl = appServerList.getUrl();
    String sendUrl = StringUtils.join(appUrl, "/send");

    LOGGER.info("Sending request to server: {}", appUrl);
    ResponseEntity<JsonNode> response = restTemplate.postForEntity(sendUrl, requestBody, JsonNode.class);

    if (response.getStatusCode().is2xxSuccessful()) {
      LOGGER.info("Received response from server: {}", appUrl);
      return response;
    } else {
      throw new Exception("App server didn't respond with 2xx.");
    }
  }

  @PostMapping(value = "/register")
  public JsonNode register(@RequestBody JsonNode requestBody) {
    String url = requestBody.get("url").asText();
    appServerList.addUrl(url);
    return requestBody;
  }

}
