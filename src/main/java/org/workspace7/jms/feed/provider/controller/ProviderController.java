package org.workspace7.jms.feed.provider.controller;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.web.bind.annotation.*;
import org.workspace7.jms.feed.provider.data.EventPayload;
import org.workspace7.jms.feed.provider.data.TriggerData;
import org.workspace7.jms.feed.provider.service.OpenWhiskAPIService;
import org.workspace7.jms.feed.provider.service.TriggerDataService;
import org.workspace7.jms.feed.provider.util.Utils;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/feed")
@Slf4j
public class ProviderController {

  private final OpenWhiskAPIService openWhiskAPIService;
  private final TriggerDataService triggerDataService;
  private final JmsListenerEndpointRegistry jmsListenerEndpointRegistry;
  private final JmsListenerContainerFactory jmsFeedContainerFactory;

  @Autowired
  public ProviderController(OpenWhiskAPIService openWhiskAPIService,
                            TriggerDataService triggerDataService,
                            JmsListenerEndpointRegistry jmsListenerEndpointRegistry,
                            JmsListenerContainerFactory jmsFeedContainerFactory) {
    this.openWhiskAPIService = openWhiskAPIService;
    this.triggerDataService = triggerDataService;
    this.jmsListenerEndpointRegistry = jmsListenerEndpointRegistry;
    this.jmsFeedContainerFactory = jmsFeedContainerFactory;

  }

  @PostConstruct
  private void reconstructListenersFromDB() {
    log.info("Reconstructing all Listeners");
    triggerDataService.findAll()
      .map(triggerData -> {
        log.info("Reconstructing Listener for trigger {} and destination {}", triggerData.getTriggerName(),
          triggerData.getDestinationName());
        return addListenerEndpointToRegistry(triggerData.getTriggerName(),
          triggerData.getDestinationName());
      }).subscribe();
  }

  /**
   * @param data
   * @return
   */
  @RequestMapping(value = "/listener", method = RequestMethod.POST, produces = "application/json")
  public ResponseEntity<String> addFeedToTrigger(@RequestBody Map<String, String> data) {

    log.info("Input Data: {}", data);

    String destinationName = "openwhisk_demo"; //Default Dead Letter Queue if not destination is sent for req

    //TODO validations
    if (!data.isEmpty()) {
      if (data.containsKey("destinationName")) {
        destinationName = data.get("destinationName");
      }

      TriggerData triggerData = buildTriggerData(data);
      triggerData.setDestinationName(destinationName);
      addListenerEndpointToRegistry(triggerData.getTriggerName(), destinationName);

      triggerDataService.saveOrUpdate(triggerData);

      final JsonObject response = new JsonObject();
      response.addProperty("status", String.valueOf(HttpStatus.OK));
      response.addProperty("message", String.format("Successfully enabled Listener for %s", destinationName));
      return ResponseEntity.ok(response.toString());
    } else {
      return ResponseEntity.badRequest().body("Request data is not valid or empty");
    }
  }

  private TriggerData buildTriggerData(Map<String, String> data) {
    return TriggerData.builder().authKey(
      data.get("authKey"))
      .triggerName(data.get("triggerName"))
      .triggerShortName(Utils.shortTriggerID(data.get("triggerName"))).build();
  }

  /**
   * @param triggerName
   * @return
   */
  @RequestMapping(value = "/listener/{triggerName}", method = RequestMethod.DELETE)
  public ResponseEntity removeFeedToTrigger(@PathVariable("triggerName") String triggerName) {
    log.info("Disassociating Trigger {}", Utils.base64Decode(triggerName));
    Optional<TriggerData> triggerData = triggerDataService.getDocumentById(triggerName);
    if (triggerData.isPresent()) {
      removeListenerEndpointFromRegistry(triggerData.get().getTriggerName());
      triggerDataService.deleleteDoc(triggerName);
    }
    return ResponseEntity.noContent().build();
  }


  /**
   * @param endpointId
   * @param destinationName
   */
  private SimpleJmsListenerEndpoint addListenerEndpointToRegistry(String endpointId, String destinationName) {
    SimpleJmsListenerEndpoint jmsListenerEndpoint = new SimpleJmsListenerEndpoint();
    jmsListenerEndpoint.setDestination(destinationName);
    jmsListenerEndpoint.setId(endpointId);
    jmsListenerEndpoint.setMessageListener(message -> {
      log.info("Recevied Message {} ", message);
      EventPayload eventPayload = null;
      try {
        eventPayload = EventPayload.builder()
          .correlationId(message.getJMSCorrelationID())
          .data(message.getBody(String.class)) //TODO need to see if this suffice or do it better
          .build();
      } catch (JMSException e) {
        //ignore
      }
      openWhiskAPIService.invokeTriggers(destinationName, eventPayload).subscribe();
    });

    if (!jmsListenerEndpointRegistry.getListenerContainerIds().contains(endpointId)) {
      jmsListenerEndpointRegistry.registerListenerContainer(jmsListenerEndpoint,
        jmsFeedContainerFactory, true);
    } else {
      MessageListenerContainer listenerContainer = jmsListenerEndpointRegistry.getListenerContainer(endpointId);
      if (listenerContainer != null && !listenerContainer.isRunning()) {
        listenerContainer.start();
      }
    }

    return jmsListenerEndpoint;
  }

  /**
   * @param endpointId
   */
  private void removeListenerEndpointFromRegistry(String endpointId) {
    MessageListenerContainer tobeDeleted = jmsListenerEndpointRegistry.getListenerContainer(endpointId);
    if (tobeDeleted != null) {
      tobeDeleted.stop(() -> {
        log.info("Successfully stopped container {}", endpointId);
      });
    }
  }
}
