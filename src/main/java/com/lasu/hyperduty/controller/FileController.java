package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseResult<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "directory", required = false, defaultValue = "") String directory) {
        
        if (file.isEmpty()) {
            return ResponseResult.error("上传文件不能为空");
        }

        String filePath = fileStorageService.uploadFile(file, directory);
        String fileUrl = fileStorageService.getFileUrl(filePath);

        Map<String, Object> result = new HashMap<>();
        result.put("fileName", file.getOriginalFilename());
        result.put("filePath", filePath);
        result.put("fileUrl", fileUrl);
        result.put("fileSize", file.getSize());
        result.put("contentType", file.getContentType());

        return ResponseResult.success(result);
    }

    @DeleteMapping("/delete")
    public ResponseResult<Void> deleteFile(@RequestParam("filePath") String filePath) {
        fileStorageService.deleteFile(filePath);
        return ResponseResult.success();
    }

    @GetMapping("/presigned-url")
    public ResponseResult<String> getPresignedUrl(
            @RequestParam("filePath") String filePath,
            @RequestParam(value = "expirationMinutes", defaultValue = "60") int expirationMinutes) {
        
        String presignedUrl = fileStorageService.getPresignedUrl(filePath, expirationMinutes);
        return ResponseResult.success(presignedUrl);
    }

    @GetMapping("/exists")
    public ResponseResult<Boolean> fileExists(@RequestParam("filePath") String filePath) {
        boolean exists = fileStorageService.fileExists(filePath);
        return ResponseResult.success(exists);
    }
}
