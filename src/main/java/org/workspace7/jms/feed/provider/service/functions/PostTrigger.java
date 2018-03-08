package org.workspace7.jms.feed.provider.service.functions;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.workspace7.jms.feed.provider.data.EventPayload;
import org.workspace7.jms.feed.provider.data.TriggerRequest;
import org.workspace7.jms.feed.provider.util.Utils;

import java.util.Collections;
import java.util.function.Function;

/**
 *
 */
@Component
@Slf4j
public class PostTrigger implements Function<TriggerRequest, JsonObject> {

  @Autowired
  RestTemplate restTemplate;

  @Override
  public JsonObject apply(TriggerRequest triggerRequest) {
    JsonObject jsonObject = new JsonObject();
    try {
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
      httpHeaders.set("Authorization", "Basic " + Utils.base64Encoded(triggerRequest.getAuth()));

      HttpEntity<EventPayload> requestEntity = new HttpEntity<>(triggerRequest.getEventPayload(), httpHeaders);

      ResponseEntity<String> response = restTemplate.exchange(triggerRequest.getUri(),
        HttpMethod.POST, requestEntity, String.class);

      log.info("Status: {}  Response body:{}", response.getStatusCode().value(), response.getBody());

      jsonObject.addProperty("done", true);
      jsonObject.addProperty("status", response.getStatusCode().toString());
      jsonObject.addProperty("response", response.getBody());

    } catch (Exception e1) {
      log.error("Error with trigger " + Utils.shortTriggerID(triggerRequest.getTriggerName()), e1);
      jsonObject.addProperty("done", false);
      jsonObject.addProperty("error", e1.getMessage());
    }
    return jsonObject;
  }
}
