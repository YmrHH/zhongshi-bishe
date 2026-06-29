package com.gzpprod.center.common;

import org.springframework.http.MediaType;

public final class FileMediaTypes {

    private FileMediaTypes() {
    }

    public static MediaType resolve(String filename) {
        if (filename == null) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        }
        if (lower.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }
        if (lower.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        if (lower.endsWith(".bmp")) {
            return MediaType.parseMediaType("image/bmp");
        }
        if (lower.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF;
        }
        if (lower.endsWith(".doc")) {
            return MediaType.parseMediaType("application/msword");
        }
        if (lower.endsWith(".docx")) {
            return MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }
        if (lower.endsWith(".xls")) {
            return MediaType.parseMediaType("application/vnd.ms-excel");
        }
        if (lower.endsWith(".xlsx")) {
            return MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        if (lower.endsWith(".ppt")) {
            return MediaType.parseMediaType("application/vnd.ms-powerpoint");
        }
        if (lower.endsWith(".pptx")) {
            return MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        }
        if (lower.endsWith(".txt")) {
            return MediaType.TEXT_PLAIN;
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
