package com.gym.controller;

import com.gym.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @PostMapping("/avatar")
    public R<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return R.fail("请选择文件");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            return R.fail("只支持JPG和PNG格式的图片");
        }

        // 检查文件大小（5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return R.fail("文件大小不能超过5MB");
        }

        try {
            // 按日期分目录存储
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String dirPath = uploadPath + "/avatar/" + dateDir;
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString().replace("-", "") + suffix;

            // 保存文件
            Path filePath = Paths.get(dirPath, newFilename);
            Files.write(filePath, file.getBytes());

            // 返回可访问的相对路径
            String avatarUrl = "/uploads/avatar/" + dateDir + "/" + newFilename;
            Map<String, String> data = new HashMap<>();
            data.put("avatar", avatarUrl);
            return R.ok(data);
        } catch (IOException e) {
            return R.fail("文件上传失败: " + e.getMessage());
        }
    }
}
