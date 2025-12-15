package com.example.resizedimagehandler;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AppSyncImgHandler {

    private final S3AsyncClient s3Client;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String APPSYNC_URL = System.getenv("APPSYNC_API_URL");
    private final String APPSYNC_API_KEY = System.getenv("APPSYNC_API_KEY");
    private final OkHttpClient httpClient = new OkHttpClient();

    public AppSyncImgHandler() {
        s3Client = DependencyFactory.s3Client();
    }

    public void pushImage(final String resizedImgBucket, String uploadedImageKey, final Context context) {
        try {
            // Create the request
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(resizedImgBucket)
                    .key(java.net.URLDecoder.decode(uploadedImageKey, "UTF-8"))
                    .build();

            // Get the object as bytes
            CompletableFuture<ResponseBytes<GetObjectResponse>> future = s3Client.getObject(
                    getObjectRequest,
                    AsyncResponseTransformer.toBytes()
            );
            
            // Wait for the result
            ResponseBytes<GetObjectResponse> responseBytes = future.get();
            // Encode to Base64
            String base64Data = Base64.getEncoder().encodeToString(responseBytes.asByteArray());
            // Prepare AppSync mutation
            Map<String, Object> appSyncMutation = getAppSyncMutation(uploadedImageKey, base64Data);

            String json = objectMapper.writeValueAsString(appSyncMutation);

            // Send mutation to AppSync
            Request request = new Request.Builder()
                    .url(APPSYNC_URL)
                    .addHeader("x-api-key", APPSYNC_API_KEY)
                    .post(okhttp3.RequestBody.create(json, MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorMsg = "AppSync mutation failed: " + response.body().string();
                    context.getLogger().log(errorMsg);
                    throw new RuntimeException(errorMsg);
                } else {
                    context.getLogger().log("Image pushed to AppSync: " + uploadedImageKey);
                }
            }
        } catch (Exception e) {
            context.getLogger().log("Pushing img to AppSync failed: " + e.getMessage());
        }
    }

    @NotNull
    private static Map<String, Object> getAppSyncMutation(String uploaded_image_key, String base64Data) {

        Map<String, Object> variables = new HashMap<>();
        variables.put("imageName", uploaded_image_key);
        variables.put("base64Data", base64Data);

        Map<String, Object> finalMap = new HashMap<>();
        String mutation = "mutation PublishImageResized($imageName: String!, $base64Data: String!) {" +
                "publishImageResized(imageName: $imageName, base64Data: $base64Data) { " +
                "imageName " +
                "base64Data" +
                " } " +
                " } ";
        finalMap.put("query", mutation);
        finalMap.put("variables", variables);
        return finalMap;
    }
}
