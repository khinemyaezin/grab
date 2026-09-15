package com.grab.storage.infrastructure;

import com.grab.framework.storage.FileStoragePort;
import com.grab.framework.storage.PresignedUpload;
import com.grab.framework.storage.PresignedUrl;
import com.grab.framework.storage.UploadRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public class S3FileStorageAdapter implements FileStoragePort {

    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final S3StorageProperties properties;

    public S3FileStorageAdapter(S3Client s3Client, S3Presigner presigner, S3StorageProperties properties) {
        this.s3Client = Objects.requireNonNull(s3Client);
        this.presigner = Objects.requireNonNull(presigner);
        this.properties = Objects.requireNonNull(properties);
    }

    @Override
    public PresignedUpload createPresignedUpload(UploadRequest request) {
        PutObjectRequest.Builder put = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(request.storageKey());
        if (request.contentType() != null && !request.contentType().isBlank()) {
            put.contentType(request.contentType());
        }
        if (request.sizeBytes() != null && request.sizeBytes() > 0) {
            put.contentLength(request.sizeBytes());
        }

        PresignedPutObjectRequest presigned = presigner.presignPutObject(PutObjectPresignRequest.builder()
                .signatureDuration(properties.presignTtl())
                .putObjectRequest(put.build())
                .build());

        Map<String, String> headers = PresignedUploadHeaders.fromSigned(
                presigned.signedHeaders(),
                request.contentType()
        );

        return new PresignedUpload(
                presigned.url().toString(),
                "PUT",
                headers,
                request.storageKey(),
                Instant.now().plus(properties.presignTtl())
        );
    }

    @Override
    public boolean objectExists(String storageKey) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(storageKey)
                    .build());
            return true;
        } catch (NoSuchKeyException ex) {
            return false;
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                return false;
            }
            throw ex;
        }
    }

    @Override
    public String resolvePublicUrl(String storageKey) {
        String base = properties.publicBaseUrl();
        if (base.endsWith("/")) {
            return base + storageKey;
        }
        return base + "/" + storageKey;
    }

    @Override
    public PresignedUrl createPresignedGet(String storageKey) {
        PresignedGetObjectRequest presigned = presigner.presignGetObject(GetObjectPresignRequest.builder()
                .signatureDuration(properties.presignTtl())
                .getObjectRequest(builder -> builder.bucket(properties.bucket()).key(storageKey))
                .build());
        return new PresignedUrl(presigned.url().toString(), Instant.now().plus(properties.presignTtl()));
    }

    @Override
    public void copy(String sourceKey, String destKey) {
        s3Client.copyObject(CopyObjectRequest.builder()
                .sourceBucket(properties.bucket())
                .sourceKey(sourceKey)
                .destinationBucket(properties.bucket())
                .destinationKey(destKey)
                .build());
    }

    @Override
    public void delete(String storageKey) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(properties.bucket())
                .key(storageKey)
                .build());
    }
}
