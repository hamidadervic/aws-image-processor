package com.example.resizedimagehandler;

import com.amazonaws.services.lambda.runtime.Context;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

import java.util.HashMap;

public class DynamoDBImgMetaHandler {

    private final String DYNAMO_DB_TABLE_FOR_IMG_META = System.getenv("DYNAMO_DB_TABLE_FOR_IMG_META");

    public DynamoDBImgMetaHandler() {}

    public void saveImgMetaData(final String imageId, final String uploadedImageKey, final Context context) {

        HashMap<String, AttributeValue> itemValues = new HashMap<>();
        itemValues.put("imageId", AttributeValue.builder().s(imageId).build());
        itemValues.put("resizedKey", AttributeValue.builder().s(uploadedImageKey).build());

        try (DynamoDbClient ddb = DynamoDbClient.builder()
                .region(Region.EU_WEST_3)
                .build()) {
            PutItemRequest dBRequest = PutItemRequest.builder()
                    .tableName(DYNAMO_DB_TABLE_FOR_IMG_META)
                    .item(itemValues)
                    .build();
            PutItemResponse response = ddb.putItem(dBRequest);
            context.getLogger().log("Table was successfully updated. The request id is: " + response.responseMetadata().requestId());
        } catch (DynamoDbException e) {
            context.getLogger().log("There was an error while putting data into table: " + (e.getMessage()));
            System.exit(1);
        }
    }
}
