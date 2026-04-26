package com.lasu.hyperduty.common.service;

import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;








public interface FileStorageService {

    String uploadFile(MultipartFile file, String directory);

    String uploadFile(InputStream inputStream, String fileName, String contentType, long size, String directory);

    InputStream downloadFile(String filePath);

    void deleteFile(String filePath);

    boolean fileExists(String filePath);

    String getFileUrl(String filePath);

    String getPresignedUrl(String filePath, int expirationMinutes);

    // 分片上传相关方法
    boolean checkFileExists(String fileHash, String fileName);

    String getFilePathByHash(String fileHash, String fileName);

    void uploadChunk(MultipartFile file, String fileHash, String fileName, int chunkIndex, int chunkCount);

    String mergeChunks(String fileHash, String fileName, int chunkCount);
}
