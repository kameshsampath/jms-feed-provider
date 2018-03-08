package org.workspace7.jms.feed.provider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class JmsFeedProviderApplicationTests {

  @Autowired
  JmsTemplate jmsTemplate;

  @Test
  public void contextLoads() {
    Map<String, Object> data = new HashMap<>();
    data.put("destinationName", "test_queue");
    data.put("triggerName", "/_/test_trigger");
    data.put("authKey", "xxxx:yyyy");

    given()
      .contentType("application/json")
      .body(data)
      .post("/api/feed/listener")
      .then()
      .assertThat()
      .statusCode(200);

    given()
      .contentType("application/json")
      .body("Hello World!")
      .get("/data/add/test_queue")
      .then()
      .assertThat()
      .statusCode(202);

  }

}
