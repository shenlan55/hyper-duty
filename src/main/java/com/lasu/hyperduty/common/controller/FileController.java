package com.lasu.hyperduty.common.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.common.service.FileStorageService;
import com.lasu.hyperduty.common.config.KKFileViewConfig;
import com.lasu.hyperduty.common.config.RustFSConfig;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;








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

    @PostMapping("/check")
    public ResponseResult<Map<String, Object>> checkFile(
            @RequestBody Map<String, Object> params) {
        
        String fileHash = (String) params.get("fileHash");
        String fileName = (String) params.get("fileName");
        
        if (fileHash == null || fileName == null) {
            return ResponseResult.error("参数不能为空");
        }

        boolean exists = fileStorageService.checkFileExists(fileHash, fileName);
        
        Map<String, Object> result = new HashMap<>();
        result.put("exists", exists);
        
        if (exists) {
            String filePath = fileStorageService.getFilePathByHash(fileHash, fileName);
            String fileUrl = "/api/file/preview?filePath=" + filePath;
            String previewUrl = generateKKFileViewUrl(filePath, fileName);
            result.put("filePath", filePath);
            result.put("fileUrl", fileUrl);
            result.put("previewUrl", previewUrl);
        }

        return ResponseResult.success(result);
    }

    @PostMapping("/upload-chunk")
    public ResponseResult<Map<String, Object>> uploadChunk(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileHash") String fileHash,
            @RequestParam("fileName") String fileName,
            @RequestParam("chunkIndex") Integer chunkIndex,
            @RequestParam("chunkCount") Integer chunkCount) {
        
        if (file.isEmpty()) {
            return ResponseResult.error("上传文件不能为空");
        }

        try {
            fileStorageService.uploadChunk(file, fileHash, fileName, chunkIndex, chunkCount);
            
            Map<String, Object> result = new HashMap<>();
            result.put("chunkIndex", chunkIndex);
            result.put("status", "success");
            
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("上传分片失败: {}", e.getMessage(), e);
            return ResponseResult.error("上传分片失败: " + e.getMessage());
        }
    }

    @PostMapping("/merge")
    public ResponseResult<Map<String, Object>> mergeChunks(
            @RequestBody Map<String, Object> params) {
        
        String fileHash = (String) params.get("fileHash");
        String fileName = (String) params.get("fileName");
        Integer chunkCount = (Integer) params.get("chunkCount");
        
        if (fileHash == null || fileName == null || chunkCount == null) {
            return ResponseResult.error("参数不能为空");
        }

        try {
            String filePath = fileStorageService.mergeChunks(fileHash, fileName, chunkCount);
            String fileUrl = "/api/file/preview?filePath=" + filePath;
            String previewUrl = generateKKFileViewUrl(filePath, fileName);

            Map<String, Object> result = new HashMap<>();
            result.put("fileName", fileName);
            result.put("filePath", filePath);
            result.put("fileUrl", fileUrl);
            result.put("previewUrl", previewUrl);

            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("合并分片失败: {}", e.getMessage(), e);
            return ResponseResult.error("合并分片失败: " + e.getMessage());
        }
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
            
            // 对文件名进行URL编码，支持中文文件名
            String encodedFileName = java.net.URLEncoder.encode(displayFileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            
            // 根据文件扩展名设置正确的Content-Type
            String contentType = getContentType(displayFileName);
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + encodedFileName + "; filename=\"" + encodedFileName + "\"");
            
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

    @GetMapping("/download")
    public void downloadFile(
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
            
            // 对文件名进行URL编码，支持中文文件名
            String encodedFileName = java.net.URLEncoder.encode(displayFileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            
            // 设置响应头为attachment，强制下载
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName + "; filename=\"" + encodedFileName + "\"");
            
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
            log.error("下载文件失败: {}", e.getMessage(), e);
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("下载文件失败");
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
     * @param fileUrl 文件路径
     * @param fileName 文件名
     * @return KKFileView预览URL
     */
    private String generateKKFileViewUrl(String fileUrl, String fileName) {
        try {
            String fullFileUrl;
            String backendUrl = System.getenv("BACKEND_URL");
            if (backendUrl == null || backendUrl.isEmpty()) {
                // 本地开发环境，用 host.docker.internal（启动 KKFileView 时加了 --add-host）
                backendUrl = "http://host.docker.internal:8080";
            }

            // 加上 &fullfilename 参数明确告诉 KKFileView 文件名！
            fullFileUrl = backendUrl + "/api/file/preview?filePath=" +
                java.net.URLEncoder.encode(fileUrl, StandardCharsets.UTF_8.toString()) +
                "&fullfilename=" + java.net.URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());

            // KKFileView 4.4.0 用 URL 安全 Base64，直接传递
            String base64Url = Base64.getUrlEncoder().encodeToString(fullFileUrl.getBytes(StandardCharsets.UTF_8));
            String finalPreviewUrl = kkFileViewConfig.getEndpoint() + kkFileViewConfig.getPreviewPath() + "?url=" + base64Url;
            return finalPreviewUrl;
        } catch (Exception e) {
            log.error("生成KKFileView预览URL失败: {}", e.getMessage(), e);
            return fileUrl;
        }
    }

    /**
     * 根据文件名获取对应的Content-Type
     * @param fileName 文件名
     * @return Content-Type字符串
     */
    private String getContentType(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "application/octet-stream";
        }
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerName.endsWith(".doc") || lowerName.endsWith(".docx")) {
            return "application/msword";
        } else if (lowerName.endsWith(".xls") || lowerName.endsWith(".xlsx")) {
            return "application/vnd.ms-excel";
        } else if (lowerName.endsWith(".ppt") || lowerName.endsWith(".pptx")) {
            return "application/vnd.ms-powerpoint";
        } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerName.endsWith(".png")) {
            return "image/png";
        } else if (lowerName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerName.endsWith(".bmp")) {
            return "image/bmp";
        } else if (lowerName.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerName.endsWith(".txt")) {
            return "text/plain";
        } else if (lowerName.endsWith(".html") || lowerName.endsWith(".htm")) {
            return "text/html";
        } else if (lowerName.endsWith(".xml")) {
            return "application/xml";
        } else if (lowerName.endsWith(".zip")) {
            return "application/zip";
        } else if (lowerName.endsWith(".rar")) {
            return "application/x-rar-compressed";
        }
        return "application/octet-stream";
    }
}
