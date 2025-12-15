package com.example.imagepresignedurl;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Lambda function entry point.
 *
 * @see <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda Java Handler</a> for more information
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final S3PresignedUrlService s3PresignedUrlService;

    public App() {
        s3PresignedUrlService = new S3PresignedUrlService();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent apiGatewayEvent, final Context context) {
        String filename = apiGatewayEvent.getQueryStringParameters().get("filename");
        String contentType = apiGatewayEvent.getQueryStringParameters().get("contentType");

        if (filename == null || contentType == null) {
           throw new IllegalArgumentException("filename and contentType required");
        }

        String url = s3PresignedUrlService.generatePresignedUrl(filename, contentType);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "http://localhost:4200"); // Angular default port
        headers.put("Access-Control-Allow-Headers", "*");
        headers.put("Access-Control-Allow-Methods", "GET,PUT,POST,OPTIONS");

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(headers)
                .withBody(String.format("{\"url\":\"%s\"}", url));
    }
}
