package router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class AppServerList {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppServerList.class);

  private final BlockingQueue<String> urlQueue = new LinkedBlockingQueue<>();

  public List<String> getAllUrl() {
    return urlQueue.stream().toList();
  }

  public synchronized void addUrl(String url) {
    if(!urlQueue.contains(url)) {
      urlQueue.add(url);
      LOGGER.info("Added url: {}", url);
    }
  }

  public synchronized String getUrl() throws Exception {
    String nextUrl = urlQueue.take();
    urlQueue.add(nextUrl);
    return nextUrl;
  }

  public void removeUrl(String url) {
    urlQueue.remove(url);
    LOGGER.info("Removed url: {}", url);
  }

}
