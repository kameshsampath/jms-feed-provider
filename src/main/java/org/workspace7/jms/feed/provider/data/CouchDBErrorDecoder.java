package org.workspace7.jms.feed.provider.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class CouchDBErrorDecoder implements ErrorDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {
    JsonObject jsonObject = new JsonObject();
    try {
      jsonObject = new JsonParser().parse(response.body().asReader()).getAsJsonObject();
      log.info("Resonse:{}" + jsonObject);
      if (jsonObject == null || !jsonObject.has("error")) {
        FeignException.errorStatus(methodKey, response);
      }
    } catch (IOException e) {
      FeignException.errorStatus(methodKey, response);
    }

    if (response.status() == 404) {
      String error = jsonObject.get("error").getAsString();
      String reason = jsonObject.get("reason").getAsString();
      if ("not_found".equalsIgnoreCase(error)) {
        if ("missing".equalsIgnoreCase(reason)) {
          return new DocumentNotFoundException(error, reason);
        } else if ("Database does not exist.".equalsIgnoreCase(reason)) {
          return new DBDoesNotExistsException(error, reason);
        }
      }

    } else if (response.status() == 412) {
      String error = jsonObject.get("error").getAsString();
      String reason = jsonObject.get("reason").getAsString();
      if ("file_exists".equalsIgnoreCase(error)) {
        return new DBAlreadyExistsException(error, reason);
      }
    }
    return FeignException.errorStatus(methodKey, response);
  }
}
