package com.example.imagepresignedurl;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

public class S3PresignedUrlService {

    private final String UPLOAD_BUCKET = System.getenv("UPLOAD_BUCKET");

    public S3PresignedUrlService() {}

    public String generatePresignedUrl(String key, String contentType) {
        try (S3Presigner s3Presigner = S3Presigner.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(UPLOAD_BUCKET)
                    .key(key)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .putObjectRequest(objectRequest)
                    .signatureDuration(Duration.ofMinutes(3))
                    .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

            return presignedRequest.url().toString();
        }
    }
}
