package com.lists.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lists.model.Thing;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

/**
 * Handler for requests to Lambda function.
 */
@Slf4j
@Singleton
public class DynamoThingStreamHandler implements RequestHandler<DynamodbEvent, Void> {

    private static final String INSERT = "INSERT";
    private static final String MODIFY = "MODIFY";
    private static final String REMOVE = "REMOVE";

    private RestHighLevelClient restHighLevelClient;
    private Gson gson;

    @Inject
    public DynamoThingStreamHandler(RestHighLevelClient restHighLevelClient, Gson gson) {
        this.restHighLevelClient = restHighLevelClient;
        this.gson = gson;
    }

    public Void handleRequest(DynamodbEvent ddbEvent, final Context context) {

        BulkRequest bulkRequest = getBulkRequest(ddbEvent);

        try {
            restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException ioException) {
            log.error("Error with bulk add", ioException);
        }

        log.info("Successfully inserted {} records", ddbEvent.getRecords().size());

        return null;
    }

    BulkRequest getBulkRequest(DynamodbEvent ddbEvent) {
        BulkRequest bulkRequest = new BulkRequest();

        log.info(ddbEvent.toString());

        for (DynamodbEvent.DynamodbStreamRecord record : ddbEvent.getRecords()) {

            Map<String, AttributeValue> newImage = record.getDynamodb().getNewImage();

            JsonObject imageJson = mapToJson(newImage);
            Thing thing = gson.fromJson(imageJson, Thing.class);

            log.info(thing.toString());

            if (INSERT.equals(record.getEventName()) || MODIFY.equals(record.getEventName())) {
                IndexRequest indexRequest = new IndexRequest(Thing.TABLE_NAME).id(thing.getThingId().toString())
                        .source(gson.toJson(thing), XContentType.JSON);

                bulkRequest.add(indexRequest);
            }
        }
        return bulkRequest;
    }

    private JsonObject mapToJson(Map<String, AttributeValue> map) {
        JsonObject jsonObject = new JsonObject();
        map.forEach((key, value) -> {
            if (value.getNULL() != null && value.isNULL())
                jsonObject.add(key, null);
            else if (value.getBOOL() != null) {
                jsonObject.addProperty(key, value.getBOOL());
            } else if (value.getS() != null) {
                jsonObject.addProperty(key, value.getS());
            } else if (value.getN() != null) {
                jsonObject.addProperty(key, new BigDecimal(value.getN()));
            } else if (value.getSS() != null) {
                JsonArray jsonArray = new JsonArray();
                value.getSS().forEach(jsonArray::add);
                jsonObject.add(key, jsonArray);
            } else if (value.getNS() != null) {
                JsonArray jsonArray = new JsonArray();
                value.getSS().forEach(element -> jsonArray.add(new BigInteger(element)));
                jsonObject.add(key, jsonArray);
            } else if (value.getM() != null) {
                jsonObject.add(key, mapToJson(value.getM()));
            }
        });

        return jsonObject;
    }
}
