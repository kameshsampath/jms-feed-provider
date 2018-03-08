package org.workspace7.jms.feed.provider.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.Base64;

@Component
@Slf4j
public class Utils {

  public static String shortTriggerID(String triggerName) {

    if (triggerName != null && triggerName.indexOf("/") != -1) {
      String[] triggerNameArray = triggerName.split("/");
      return triggerNameArray[triggerNameArray.length - 1];
    }

    return triggerName;
  }

  public static String base64Encoded(String text) {
    byte[] encodedText = Base64.getEncoder().encode(
      text.getBytes(Charset.forName("US-ASCII")));
    return new String(encodedText);
  }

  public static String base64Decode(String text) {
    byte[] decodedText = Base64.getDecoder().decode(
      text.getBytes(Charset.forName("US-ASCII")));
    return new String(decodedText);
  }
}
