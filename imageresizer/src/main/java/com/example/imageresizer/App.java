package com.example.imageresizer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;

import software.amazon.awssdk.services.s3.S3Client;

import java.util.Optional;

/**
 * Lambda function entry point.
 *
 * @see <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda Java Handler</a> for more information
 */
public class App implements RequestHandler<S3EventNotification, Object> {

    private S3Client s3Client;
    private final ImageResizerService imageResizerService;

    public App() {
        this.imageResizerService = new ImageResizerService();
    }

    @Override
    public String handleRequest(final S3EventNotification event, final Context context) {
        S3Client s3Client = getS3Client(context);

        try {
            Optional<S3EventNotification.S3EventNotificationRecord> record = event.getRecords().stream().findFirst();

            if (!record.isPresent()) return "Error";

            imageResizerService.resizeAndUpload(s3Client, context.getLogger(), record.get());
            return "Success";
        } catch (Exception e) {
            context.getLogger().log("Failed to process the image: " + e.getMessage());
            return "Error";
        }
    }

    private S3Client getS3Client(Context context) {
        if (s3Client != null) {
            return s3Client;
        }

        try {
            s3Client = S3Client.builder().build(); // Uses default credentials & region
            return s3Client;
        } catch (Exception e) {
            context.getLogger().log("Failed to create S3Client: " + e.getMessage());
            throw e; // rethrow so Lambda fails visibly
        }
    }
}
