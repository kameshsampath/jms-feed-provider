package org.workspace7.jms.feed.provider.data;

import com.google.gson.JsonObject;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

/**
 *
 */
public interface CouchDBClient {

  @RequestLine("PUT /{db}")
  public JsonObject createDB(@Param("db") String db) throws DBAlreadyExistsException;

  @RequestLine("DELETE /{db}")
  public JsonObject deleteDB(@Param("db") String db) throws DBDoesNotExistsException;

  @RequestLine("POST /{db}/_find")
  @Headers("Content-Type: application/json")
  public JsonObject allDocs(@Param("db") String db, JsonObject request);

  @RequestLine("PUT /{db}/{docid}")
  @Headers("Content-Type: application/json")
  public JsonObject saveDoc(@Param("db") String db,
                            @Param("docid") String docid,
                            TriggerData doc);

  @RequestLine("DELETE /{db}/{docid}?rev={rev}")
  @Headers("Content-Type: application/json")
  public JsonObject deleteDoc(@Param("db") String db,
                              @Param("docid") String docid,
                              @Param("rev") String rev);

  @RequestLine("GET /{db}/{docid}")
  @Headers("Content-Type: application/json")
  public JsonObject getDocumentById(@Param("db") String db,
                                    @Param("docid") String docid) throws DocumentNotFoundException;
}
