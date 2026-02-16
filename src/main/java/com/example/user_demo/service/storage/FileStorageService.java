package com.example.user_demo.service.storage;

import com.example.user_demo.enums.ErrorCode;
import com.example.user_demo.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


@Slf4j
@Service
public class FileStorageService {
    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload-dir}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String saveAvatar(Long userId, MultipartFile file){
        if(file == null || file.isEmpty()){
            throw new AppException(ErrorCode.BAD_REQUEST,"File is empty");
        }

        String contentType = file.getContentType();
        if(contentType == null || !( contentType.equals("image/png") || contentType.equals("image/jpeg") || contentType.equals("image/jpg"))){
            throw new AppException(ErrorCode.BAD_REQUEST,"Only PNG/JPG is allowed");
        }

        String ext = contentType.equals("image/png") ? "png" : "jpg";
        String filenname = "u" + userId + "-" + UUID.randomUUID() + "." + ext;

        try {
            Files.createDirectories(uploadDir);
            Path target = uploadDir.resolve(filenname).normalize();
            file.transferTo(target);
            return filenname;
        }
        catch (IOException e){
            throw new AppException(ErrorCode.INTERNAL_ERROR,"Cannot store file");
        }
    }
    public void deleteAvatar(String filename){
        if(filename == null || filename.isBlank()) return;
        try {
            Path filepath = uploadDir.resolve(filename).normalize();

            Files.deleteIfExists(filepath);
        } catch (IOException e) {
            log.error(String.valueOf(e));
        }
    }
}
