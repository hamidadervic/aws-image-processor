package com.example.imageresizer;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import net.coobird.thumbnailator.Thumbnails;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

public class ImageResizerService {

    private final String DESTINATION_BUCKET = System.getenv("DESTINATION_BUCKET");

    public void resizeAndUpload(S3Client s3Client, LambdaLogger logger, S3EventNotification.S3EventNotificationRecord s3Record) throws IOException {

        String bucket = s3Record.getS3().getBucket().getName();
        String uploaded_image_key = s3Record.getS3().getObject().getKey();

        logger.log("Processing uploaded image: " + uploaded_image_key);

        InputStream originalImage = s3Client
                .getObject(GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(uploaded_image_key)
                        .build()
                );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(new BufferedInputStream(originalImage))
                .size(300, 300)
                .outputFormat("jpg")
                .toOutputStream(outputStream);

        InputStream resizedStream = new ByteArrayInputStream(outputStream.toByteArray());
        String resizedKey = "resized-" + uploaded_image_key;

        logger.log("Name of resized image: " + resizedKey);

        s3Client.putObject(PutObjectRequest.builder()
                .bucket(DESTINATION_BUCKET)
                .key(resizedKey)
                .contentType("image/jpeg")
                .build(), RequestBody.fromInputStream(resizedStream, outputStream.size()));

        logger.log("Thumbnail uploaded to: " + DESTINATION_BUCKET);
    }
}
