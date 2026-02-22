package com.lasu.hyperduty.service.impl;

import com.lasu.hyperduty.config.RustFSConfig;
import com.lasu.hyperduty.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RustFSFileStorageServiceImpl implements FileStorageService {

    private final S3Client s3Client;
    private final RustFSConfig rustFSConfig;

    @Override
    public String uploadFile(MultipartFile file, String directory) {
        try {
            String fileName = generateFileName(file.getOriginalFilename(), directory);
            
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(rustFSConfig.getBucketName())
                    .key(fileName)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, 
                    software.amazon.awssdk.core.sync.RequestBody.fromInputStream(
                            file.getInputStream(), file.getSize()));

            log.info("文件上传成功: {}", fileName);
            return fileName;
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String fileName, String contentType, long size, String directory) {
        try {
            String key = generateFileName(fileName, directory);
            
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(rustFSConfig.getBucketName())
                    .key(key)
                    .contentType(contentType)
                    .contentLength(size)
                    .build();

            s3Client.putObject(putObjectRequest, 
                    software.amazon.awssdk.core.sync.RequestBody.fromInputStream(inputStream, size));

            log.info("文件上传成功: {}", key);
            return key;
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream downloadFile(String filePath) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(rustFSConfig.getBucketName())
                    .key(filePath)
                    .build();

            return s3Client.getObject(getObjectRequest);
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(rustFSConfig.getBucketName())
                    .key(filePath)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("文件删除成功: {}", filePath);
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean fileExists(String filePath) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(rustFSConfig.getBucketName())
                    .key(filePath)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("检查文件是否存在失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getFileUrl(String filePath) {
        return rustFSConfig.getEndpoint() + "/" + rustFSConfig.getBucketName() + "/" + filePath;
    }

    @Override
    public String getPresignedUrl(String filePath, int expirationMinutes) {
        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(
                    rustFSConfig.getAccessKey(),
                    rustFSConfig.getSecretKey()
            );

            S3Presigner presigner = S3Presigner.builder()
                    .endpointOverride(URI.create(rustFSConfig.getEndpoint()))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .region(Region.of(rustFSConfig.getRegion()))
                    .build();

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(rustFSConfig.getBucketName())
                    .key(filePath)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (Exception e) {
            log.error("生成预签名URL失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成预签名URL失败: " + e.getMessage(), e);
        }
    }

    private String generateFileName(String originalFileName, String directory) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String fileName = uuid + extension;
        
        if (directory != null && !directory.isEmpty()) {
            return directory + "/" + fileName;
        }
        return fileName;
    }

    public void ensureBucketExists() {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(rustFSConfig.getBucketName())
                    .build();

            s3Client.headBucket(headBucketRequest);
            log.info("存储桶已存在: {}", rustFSConfig.getBucketName());
        } catch (NoSuchBucketException e) {
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                    .bucket(rustFSConfig.getBucketName())
                    .build();
            s3Client.createBucket(createBucketRequest);
            log.info("存储桶创建成功: {}", rustFSConfig.getBucketName());
        } catch (Exception e) {
            log.error("检查/创建存储桶失败: {}", e.getMessage(), e);
        }
    }
}
