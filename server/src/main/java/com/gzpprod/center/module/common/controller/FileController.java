package com.gzpprod.center.module.common.controller;

import com.gzpprod.center.common.BusinessException;
import com.gzpprod.center.common.Result;
import com.gzpprod.center.config.UploadStorage;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final UploadStorage uploadStorage;

    @PostMapping("/upload")
    public Result<UploadResponse> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Result.fail("文件不能为空");
        }
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String saved = UUID.randomUUID() + "_" + original.replaceAll("[^a-zA-Z0-9._\\u4e00-\\u9fa5-]", "_");

        Path root = uploadStorage.root();
        Path target = root.resolve(saved).normalize();
        if (!target.startsWith(root)) {
            throw new BusinessException("非法文件路径");
        }

        try (var in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        return Result.ok(UploadResponse.builder()
                .fileUrl("/uploads/" + saved)
                .fileName(original)
                .build());
    }

    @Data
    @Builder
    public static class UploadResponse {
        private String fileUrl;
        private String fileName;
    }
}
