package com.example.resizedimagehandler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;

import java.util.Optional;
import java.util.UUID;

/**
 * Lambda function entry point.
 *
 * @see <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda Java Handler</a> for more information
 */
public class App implements RequestHandler<S3EventNotification, String> {

    private final DynamoDBImgMetaHandler dynamoDBImgMetaHandler;
    private final AppSyncImgHandler appSyncImgHandler;

    public App() {
        dynamoDBImgMetaHandler = new DynamoDBImgMetaHandler();
        appSyncImgHandler = new AppSyncImgHandler();
    }

    @Override
    public String handleRequest(final S3EventNotification s3Event, final Context context) {
        try {
            Optional<S3EventNotification.S3EventNotificationRecord> s3Record = s3Event.getRecords().stream().findFirst();

            if (!s3Record.isPresent()) {
                context.getLogger().log("[ResizedImageHandler] No record found for resized image.");
                return "Error";
            }

            String imageId = UUID.randomUUID().toString();
            String bucketName = s3Record.get().getS3().getBucket().getName();
            String uploadedImageKey = s3Record.get().getS3().getObject().getKey();

            dynamoDBImgMetaHandler.saveImgMetaData(imageId, uploadedImageKey, context);
            appSyncImgHandler.pushImage(bucketName, uploadedImageKey, context);

            return "Success";
        } catch (Exception e) {
            context.getLogger().log("Failed to process the image meta: " + e.getMessage());
            return "Error";
        }
    }
}
