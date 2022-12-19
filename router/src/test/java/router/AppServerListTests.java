package router;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppServerListTests {

  private AppServerList appServerList;

  @BeforeEach
  public void init() {
    appServerList = new AppServerList();
  }

  @Test
  public void whenInsertSameUrl_thenNoDuplicates() {
    appServerList.addUrl("url1");
    appServerList.addUrl("url1");

    int urlListSize = appServerList.getAllUrl().size();

    assertEquals(1, urlListSize);
  }

  @Test
  public void whenGettingAUrl_thenItIsEnqueuedAgain() throws Exception {
    appServerList.addUrl("url1");
    appServerList.addUrl("url2");

    String firstUrl = appServerList.getUrl();
    String secondUrl = appServerList.getUrl();
    String thirdUrl = appServerList.getUrl();

    assertEquals(firstUrl, "url1");
    assertEquals(secondUrl, "url2");
    assertEquals(thirdUrl, "url1");
  }

  @Test
  public void whenAUrlIsRemoved_thenItIsNotDequeued() throws Exception {
    appServerList.addUrl("url1");
    appServerList.addUrl("url2");
    appServerList.addUrl("url3");

    appServerList.removeUrl("url2");

    String firstUrl = appServerList.getUrl();
    String secondUrl = appServerList.getUrl();

    assertEquals(firstUrl, "url1");
    assertEquals(secondUrl, "url3");
  }

}
