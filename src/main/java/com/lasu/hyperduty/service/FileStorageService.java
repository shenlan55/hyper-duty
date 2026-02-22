package com.lasu.hyperduty.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileStorageService {

    String uploadFile(MultipartFile file, String directory);

    String uploadFile(InputStream inputStream, String fileName, String contentType, long size, String directory);

    InputStream downloadFile(String filePath);

    void deleteFile(String filePath);

    boolean fileExists(String filePath);

    String getFileUrl(String filePath);

    String getPresignedUrl(String filePath, int expirationMinutes);
}
