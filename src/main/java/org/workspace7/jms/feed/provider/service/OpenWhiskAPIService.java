package org.workspace7.jms.feed.provider.service;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.workspace7.jms.feed.provider.config.OpenWhiskProperties;
import org.workspace7.jms.feed.provider.data.EventPayload;
import org.workspace7.jms.feed.provider.data.TriggerData;
import org.workspace7.jms.feed.provider.data.TriggerRequest;
import org.workspace7.jms.feed.provider.service.functions.PostTrigger;
import org.workspace7.jms.feed.provider.util.Utils;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.net.URISyntaxException;

@Service
@Slf4j
public class OpenWhiskAPIService {

  @Autowired
  private OpenWhiskProperties openWhiskProperties;

  @Autowired
  TriggerDataService triggerDataService;

  @Autowired
  private PostTrigger postTrigger;

  /**
   *
   * @param destinationName
   * @param payload
   * @return
   */
  public Flux<JsonObject> invokeTriggers(String destinationName, EventPayload payload) {
    log.info("Invoking Triggers with Payload {} ", payload);
    Flux<JsonObject> triggerResponses;
    Flux<TriggerData> fluxOfTriggers = triggerDataService.findAllByDestination(destinationName);

    triggerResponses = fluxOfTriggers.map(triggerData -> {
      TriggerRequest.TriggerRequestBuilder requestBuilder = TriggerRequest.builder();
      try {
        return requestBuilder
          .triggerName(triggerData.getTriggerName())
          .eventPayload(payload)
          .auth(triggerData.getAuthKey())
          .uri(
            new URI(openWhiskProperties.getApiHost() + "/" + Utils.shortTriggerID(triggerData.getTriggerName())))
          .build();
      } catch (URISyntaxException e) {
        return null;
      }
    }).filter(triggerRequest -> triggerRequest != null)
      .map(postTrigger);
    return triggerResponses;
  }
}
