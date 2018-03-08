package org.workspace7.jms.feed.provider.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.workspace7.jms.feed.provider.data.CouchDBClient;
import org.workspace7.jms.feed.provider.data.DBAlreadyExistsException;
import org.workspace7.jms.feed.provider.data.DocumentNotFoundException;
import org.workspace7.jms.feed.provider.data.TriggerData;
import org.workspace7.jms.feed.provider.util.Utils;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class TriggerDataService {

  private final Gson gson;
  private CouchDBClient couchDBClient;

  @Value("${openwhisk.dbName}")
  private String dbName;

  private static final String[] SELECTOR_FIELDS = {"authKey", "triggerName", "destinationName", "triggerShortName"};

  public TriggerDataService(CouchDBClient couchDBClient, Gson gson) {
    this.couchDBClient = couchDBClient;
    this.gson = gson;
  }

  @PostConstruct
  protected void init() {
    try {
      JsonObject response = couchDBClient.createDB(dbName);
      Objects.equals(response.get("ok").getAsBoolean(), true);
    } catch (DBAlreadyExistsException e) {
      log.info("DB {} already exists, skipping creation", dbName);
    }
  }

  /**
   * @param triggerData
   * @return
   */
  public boolean saveOrUpdate(TriggerData triggerData) {
    String docId = Utils.base64Encoded(triggerData.getTriggerName());
    JsonObject doc = null;
    log.info("Saving Document {} with ID {}", triggerData, docId);
    Optional<TriggerData> optionalTriggerData = getDocumentById(docId);
    if (optionalTriggerData.isPresent()) {
      String revision = optionalTriggerData.get().getRevision();
      triggerData.setRevision(revision);
      doc = couchDBClient.saveDoc(dbName, docId, triggerData);
      log.info("Updated Document {}", doc);
    } else {
      doc = couchDBClient.saveDoc(dbName, docId, triggerData);
      log.info("Saved Document {}", doc);
    }
    return doc != null ? doc.get("ok").getAsBoolean() : false;
  }

  /**
   * @param docId
   * @return
   */
  public boolean deleleteDoc(String docId) {
    Optional<TriggerData> optionalTriggerData = getDocumentById(docId);
    if (optionalTriggerData.isPresent()) {
      String revision = optionalTriggerData.get().getRevision();
      JsonObject response = couchDBClient.deleteDoc(dbName, docId, revision);
      return response != null ? response.get("ok").getAsBoolean() : false;
    } else {
      log.warn("Document with ID {} not found in DB {}", docId, dbName);
      return false;
    }
  }

  /**
   * @param docId
   * @return
   */
  public Optional<TriggerData> getDocumentById(String docId) {
    log.info("Getting Document with ID {} ", docId);
    TriggerData triggerData = null;
    try {
      JsonObject doc = couchDBClient.getDocumentById(dbName, docId);
      log.info("Document Retrieved {}", doc);
      if (doc != null) {
        triggerData = gson.fromJson(doc, TriggerData.class);
      }
    } catch (DocumentNotFoundException e) {
      log.warn("Document with ID {} not found in DB {}", docId, dbName);
    }
    return Optional.ofNullable(triggerData);
  }

  /**
   * TODO - need to optimize for getting reactively with backpressure
   *
   * @return
   */
  public Flux<TriggerData> findAll() {
    log.info("Finding All Documents");
    String regEx = "^(.*)";
    String fieldName = "triggerName";
    JsonObject request = requestSelector(fieldName, regEx);
    JsonObject response = couchDBClient.allDocs(dbName, request);
    return extractDocs(response, "Got {} documents");
  }

  private Flux<TriggerData> extractDocs(JsonObject response, String s) {
    if (response.has("docs")) {
      JsonArray jsonElements = response.get("docs").getAsJsonArray();
      log.debug(s, jsonElements.size());
      return Flux.fromIterable(jsonElements).map(e -> {
        log.debug("JSON Element {}", e);
        return gson.fromJson(e, TriggerData.class);
      });
    } else {
      return Flux.empty();
    }
  }

  /**
   * @return
   */
  private JsonObject requestSelector(String fieldName, String regEx) {
    JsonObject request = new JsonObject();
    JsonObject triggerNameSelector = new JsonObject();
    JsonObject triggerRegEx = new JsonObject();
    triggerRegEx.addProperty("$regex", regEx);
    triggerNameSelector.add(fieldName, triggerRegEx);
    request.add("selector", triggerNameSelector);
    JsonArray fields = new JsonArray();
    for (String field : SELECTOR_FIELDS) {
      fields.add(field);
    }
    return request;
  }


  public Flux<TriggerData> findAllByDestination(String destinationName) {
    log.info("Finding All triggers for Destination {} ", destinationName);
    String fieldName = "destinationName";
    JsonObject request = requestSelector(fieldName, destinationName);
    log.info("find query {}" + request);
    JsonObject response = couchDBClient.allDocs(dbName, request);
    return extractDocs(response, "Got {} documents for query by destination");
  }
}
