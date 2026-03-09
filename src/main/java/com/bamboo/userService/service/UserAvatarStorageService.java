package com.bamboo.userService.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class UserAvatarStorageService {

    private static final Map<String, String> CONTENT_TYPE_TO_EXTENSION =
            Map.of(
                    "image/jpeg", ".jpg",
                    "image/png", ".png",
                    "image/webp", ".webp",
                    "image/gif", ".gif");

    private final S3Client s3Client;
    private final HttpClient httpClient;
    private final String bucket;
    private final String baseUrl;

    public UserAvatarStorageService(
            S3Client s3Client,
            @Value("${s3.bucket.name}") String bucket,
            @Value("${s3.bucket.baseUrl}") String baseUrl) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public String storeExternalAvatar(String sourceUrl, UUID userId) {
        if (sourceUrl == null || sourceUrl.isBlank()) {
            return null;
        }

        if (sourceUrl.startsWith(baseUrl + "/" + bucket + "/")) {
            return sourceUrl;
        }

        try {
            if (sourceUrl.startsWith("data:image/")) {
                return storeDataUrlAvatar(sourceUrl, userId);
            }

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(sourceUrl))
                            .timeout(Duration.ofSeconds(20))
                            .GET()
                            .build();

            HttpResponse<byte[]> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException(
                        "Failed to download avatar. Status: " + response.statusCode());
            }

            String contentType =
                    response.headers()
                            .firstValue("Content-Type")
                            .map(value -> value.split(";")[0].trim().toLowerCase())
                            .orElse("image/jpeg");

            String extension = resolveExtension(sourceUrl, contentType);
            String key = "avatars/" + userId + "/" + UUID.randomUUID() + extension;

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(response.body()));

            return baseUrl + "/" + bucket + "/" + key;
        } catch (Exception ex) {
            return sourceUrl;
        }
    }

    private String storeDataUrlAvatar(String dataUrl, UUID userId) throws IOException {
        int commaIndex = dataUrl.indexOf(',');
        if (commaIndex < 0) {
            throw new IOException("Invalid data URL");
        }

        String metadata = dataUrl.substring(0, commaIndex);
        String payload = dataUrl.substring(commaIndex + 1);

        String contentType = metadata.substring("data:".length(), metadata.indexOf(';')).trim();
        byte[] bytes;

        if (metadata.contains(";base64")) {
            bytes = Base64.getDecoder().decode(payload);
        } else {
            bytes = payload.getBytes(StandardCharsets.UTF_8);
        }

        String extension = CONTENT_TYPE_TO_EXTENSION.getOrDefault(contentType, ".jpg");
        String key = "avatars/" + userId + "/" + UUID.randomUUID() + extension;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(bytes));

        return baseUrl + "/" + bucket + "/" + key;
    }

    private String resolveExtension(String sourceUrl, String contentType)
            throws URISyntaxException {
        String path = new URI(sourceUrl).getPath();
        if (path != null) {
            int lastDot = path.lastIndexOf('.');
            if (lastDot >= 0 && lastDot < path.length() - 1) {
                return path.substring(lastDot);
            }
        }

        return CONTENT_TYPE_TO_EXTENSION.getOrDefault(contentType, ".jpg");
    }
}
