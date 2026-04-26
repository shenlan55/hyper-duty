package com.lasu.hyperduty.common.service.impl;

import com.lasu.hyperduty.common.service.FileStorageService;
import com.lasu.hyperduty.common.config.RustFSConfig;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.S3Client;








@Slf4j
@Service
@RequiredArgsConstructor
public class RustFSFileStorageServiceImpl implements FileStorageService {

    private final S3Client s3Client;
    private final RustFSConfig rustFSConfig;
    
    // 存储分片上传的上传ID
    private final Map<String, String> uploadIdMap = new ConcurrentHashMap<>();
    // 存储分片的ETag
    private final Map<String, List<CompletedPart>> partsMap = new ConcurrentHashMap<>();

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

    @Override
    public boolean checkFileExists(String fileHash, String fileName) {
        String filePath = generateFilePathFromHash(fileHash, fileName);
        return fileExists(filePath);
    }

    @Override
    public String getFilePathByHash(String fileHash, String fileName) {
        return generateFilePathFromHash(fileHash, fileName);
    }

    @Override
    public void uploadChunk(MultipartFile file, String fileHash, String fileName, int chunkIndex, int chunkCount) {
        try {
            String key = generateFilePathFromHash(fileHash, fileName);
            String uploadKey = fileHash + "_" + fileName;
            
            // 获取或创建上传ID
            String uploadId = uploadIdMap.computeIfAbsent(uploadKey, k -> {
                CreateMultipartUploadRequest createRequest = CreateMultipartUploadRequest.builder()
                        .bucket(rustFSConfig.getBucketName())
                        .key(key)
                        .contentType(file.getContentType())
                        .build();
                CreateMultipartUploadResponse createResponse = s3Client.createMultipartUpload(createRequest);
                return createResponse.uploadId();
            });
            
            // 上传分片
            UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                    .bucket(rustFSConfig.getBucketName())
                    .key(key)
                    .uploadId(uploadId)
                    .partNumber(chunkIndex + 1) // S3 part number starts from 1
                    .contentLength(file.getSize())
                    .build();
            
            UploadPartResponse uploadPartResponse = s3Client.uploadPart(uploadPartRequest, 
                    software.amazon.awssdk.core.sync.RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            // 存储分片ETag
            List<CompletedPart> parts = partsMap.computeIfAbsent(uploadKey, k -> new ArrayList<>());
            parts.add(CompletedPart.builder()
                    .partNumber(chunkIndex + 1)
                    .eTag(uploadPartResponse.eTag())
                    .build());
            
            log.info("分片上传成功: fileHash={}, chunkIndex={}", fileHash, chunkIndex);
        } catch (Exception e) {
            log.error("分片上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("分片上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String mergeChunks(String fileHash, String fileName, int chunkCount) {
        try {
            String key = generateFilePathFromHash(fileHash, fileName);
            String uploadKey = fileHash + "_" + fileName;
            
            String uploadId = uploadIdMap.get(uploadKey);
            List<CompletedPart> parts = partsMap.get(uploadKey);
            
            if (uploadId == null || parts == null || parts.size() != chunkCount) {
                throw new RuntimeException("分片上传不完整");
            }
            
            // 按分片序号排序
            parts.sort(Comparator.comparingInt(CompletedPart::partNumber));
            
            // 完成上传
            CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
                    .bucket(rustFSConfig.getBucketName())
                    .key(key)
                    .uploadId(uploadId)
                    .multipartUpload(CompletedMultipartUpload.builder()
                            .parts(parts)
                            .build())
                    .build();
            
            s3Client.completeMultipartUpload(completeRequest);
            
            // 清理临时数据
            uploadIdMap.remove(uploadKey);
            partsMap.remove(uploadKey);
            
            log.info("文件合并成功: {}", key);
            return key;
        } catch (Exception e) {
            log.error("文件合并失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件合并失败: " + e.getMessage(), e);
        }
    }
    
    private String generateFilePathFromHash(String fileHash, String fileName) {
        String extension = "";
        if (fileName != null && fileName.contains(".")) {
            extension = fileName.substring(fileName.lastIndexOf("."));
        }
        return "chunks/" + fileHash + extension;
    }
}
