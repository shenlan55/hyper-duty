package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.config.KKFileViewConfig;
import com.lasu.hyperduty.config.RustFSConfig;
import com.lasu.hyperduty.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;
    private final KKFileViewConfig kkFileViewConfig;
    private final RustFSConfig rustFSConfig;

    @PostMapping("/upload")
    public ResponseResult<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "directory", required = false, defaultValue = "") String directory) {
        
        if (file.isEmpty()) {
            return ResponseResult.error("上传文件不能为空");
        }

        String filePath = fileStorageService.uploadFile(file, directory);
        String fileUrl = "/api/file/preview?filePath=" + filePath;
        String previewUrl = generateKKFileViewUrl(filePath, file.getOriginalFilename());

        Map<String, Object> result = new HashMap<>();
        result.put("fileName", file.getOriginalFilename());
        result.put("filePath", filePath);
        result.put("fileUrl", fileUrl);
        result.put("previewUrl", previewUrl);
        result.put("fileSize", file.getSize());
        result.put("contentType", file.getContentType());

        return ResponseResult.success(result);
    }

    @GetMapping("/preview")
    public void previewFile(
            @RequestParam("filePath") String filePath,
            @RequestParam(value = "fileName", required = false) String fileName,
            HttpServletResponse response) {
        
        try {
            // 检查文件是否存在
            if (!fileStorageService.fileExists(filePath)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 获取文件输入流
            InputStream inputStream = fileStorageService.downloadFile(filePath);
            
            // 确定文件名
            String displayFileName = fileName;
            if (displayFileName == null || displayFileName.isEmpty()) {
                displayFileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            }
            
            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "inline; filename=\"" + displayFileName + "\"");
            
            // 写入响应流
            byte[] buffer = new byte[1024 * 4];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, len);
            }
            
            // 关闭流
            inputStream.close();
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (Exception e) {
            log.error("预览文件失败: {}", e.getMessage(), e);
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("预览文件失败");
                response.getWriter().flush();
                response.getWriter().close();
            } catch (IOException ex) {
                log.error("写入响应失败: {}", ex.getMessage(), ex);
            }
        }
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

    /**
     * 生成KKFileView预览URL
     * @param fileUrl 文件访问URL
     * @param fileName 文件名
     * @return KKFileView预览URL
     */
    private String generateKKFileViewUrl(String fileUrl, String fileName) {
        try {
            log.info("RustFS Endpoint: {}", rustFSConfig.getEndpoint());
            log.info("RustFS Bucket: {}", rustFSConfig.getBucketName());
            String fullFileUrl = rustFSConfig.getEndpoint() + "/" + rustFSConfig.getBucketName() + "/" + fileUrl;
            log.info("Generated File URL: {}", fullFileUrl);
            byte[] urlBytes = fullFileUrl.getBytes(StandardCharsets.UTF_8);
            String base64Url = Base64.getEncoder().encodeToString(urlBytes);
            String previewUrl = kkFileViewConfig.getEndpoint() + kkFileViewConfig.getPreviewPath() + "?url=" + base64Url;
            log.info("Generated Preview URL: {}", previewUrl);
            return previewUrl;
        } catch (Exception e) {
            log.error("生成KKFileView预览URL失败: {}", e.getMessage(), e);
            return fileUrl;
        }
    }
}
