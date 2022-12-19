package router;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class HealthChecker {

  private static final Logger LOGGER = LoggerFactory.getLogger(HealthChecker.class);

  @Autowired
  private AppServerList appServerList;

  private Map<String, Integer> urlFailCnt = new HashMap<>();

  @Scheduled(fixedRate = 5000)
  public void doAppHealthCheck() throws Exception {
    // Send health check to all servers concurrently
    List<CompletableFuture<Pair<String, Boolean>>> completableFutures = appServerList
      .getAllUrl()
      .stream()
      .map(url -> {
        return CompletableFuture.supplyAsync(() -> sendHealthCheck(url));
      })
      .collect(Collectors.toList());

    List<Pair<String, Boolean>> healthCheckResults = CompletableFuture
      .allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]))
      .thenApply(t -> {
        return completableFutures
          .stream()
          .map(CompletableFuture::join)
          .collect(Collectors.toList());
      })
      .get();

    // Increment failure count
    int failedCnt = 0, successCnt = 0;
    for(Pair<String, Boolean> result : healthCheckResults) {
      if(result.getRight()) {
        successCnt++;
      } else {
        failedCnt++;
        int count = urlFailCnt.computeIfAbsent(result.getLeft(), s -> 0);
        urlFailCnt.put(result.getLeft(), count + 1);
      }
    }

    LOGGER.info("Performed app health check. {} success, {} failed.", successCnt, failedCnt);

    // Remove unhealthy app servers
    List<String> urlToRemoveList = urlFailCnt.entrySet()
      .stream()
      .filter(e -> e.getValue() >= 3)
      .map(e -> e.getKey())
      .collect(Collectors.toList());

    for(String url : urlToRemoveList) {
      appServerList.removeUrl(url);
      urlFailCnt.remove(url);
    }
  }

  private Pair<String, Boolean> sendHealthCheck(String url) {
    String sendUrl = StringUtils.join(url, "/health");

    RestTemplate restTemplate = new RestTemplate();

    try {
      ResponseEntity<JsonNode> response = restTemplate.getForEntity(sendUrl, JsonNode.class);
      boolean isHealthy = response.getStatusCode() == HttpStatus.OK;

      return Pair.of(url, isHealthy);
    } catch(Exception e) {
      return Pair.of(url, false);
    }
  }

}
