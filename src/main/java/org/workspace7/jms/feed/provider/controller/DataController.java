package org.workspace7.jms.feed.provider.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;

import javax.jms.Session;

@RestController
@Slf4j
@RequestMapping("/data")
public class DataController {

  JmsTemplate jmsTemplate;

  public DataController(JmsTemplate jmsTemplate) {
    this.jmsTemplate = jmsTemplate;
  }

  @RequestMapping(value = "/add/{destinationName}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void putTestData(@PathVariable("destinationName") String destinationName, @RequestBody String data) {
    try {
      jmsTemplate.send(destinationName, session ->
        session.createTextMessage(data != null ? data : String.valueOf(System.currentTimeMillis())));
    } catch (Exception e) {
      log.error("Error Sending message to destination :" + destinationName, e);
    }
  }
}
