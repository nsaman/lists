package com.lists.handler;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lists.config.AWSRequestSigningApacheInterceptor;
import com.lists.model.Thing;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

/**
 * Handler for requests to Lambda function.
 */
@Slf4j
public class CreateElaticIndexesHandler implements RequestHandler<DynamodbEvent, Void> {
    private static final String ELASTIC_SEARCH_NAME = "es";

    private RestHighLevelClient restHighLevelClient;

    public CreateElaticIndexesHandler() {
        String elasticEndpoint = System.getenv("elasticEndpoint");
        String region = System.getenv("AWS_REGION");
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName(ELASTIC_SEARCH_NAME);
        signer.setRegionName(region);
        HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(ELASTIC_SEARCH_NAME, signer, new DefaultAWSCredentialsProviderChain());
        restHighLevelClient = new RestHighLevelClient(RestClient.builder(HttpHost.create(elasticEndpoint)).setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
    }

    // for testing
    public CreateElaticIndexesHandler(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    public Void handleRequest(DynamodbEvent ddbEvent, final Context context) {

        try {
            restHighLevelClient.indices().create(new CreateIndexRequest("thing"), RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("failed to create indexes", e);
        }

        return null;
    }
}