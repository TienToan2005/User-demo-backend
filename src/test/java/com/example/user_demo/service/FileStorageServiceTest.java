package com.example.user_demo.service;

import com.example.user_demo.enums.ErrorCode;
import com.example.user_demo.exception.AppException;
import com.example.user_demo.service.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString());
    }

    @Test
    void saveAvatar_success_png_shouldStoreFile_andReturnFilename() throws IOException {
        // Arrange
        long userId = 5L;
        byte[] content = "fake-png-content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "avatar.png",
                "image/png",
                content
        );

        // Act
        String filename = fileStorageService.saveAvatar(userId, file);

        // Assert
        assertNotNull(filename);
        assertTrue(filename.startsWith("u" + userId + "-"));
        assertTrue(filename.endsWith(".png"));

        Path stored = tempDir.resolve(filename);
        assertTrue(Files.exists(stored));
        assertArrayEquals(content, Files.readAllBytes(stored));
    }

    @Test
    void saveAvatar_success_jpeg_shouldStoreFile_andReturnFilenameJpg() {
        // Arrange
        long userId = 10L;
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "avatar.jpeg",
                "image/jpeg",
                "fake-jpg".getBytes()
        );

        // Act
        String filename = fileStorageService.saveAvatar(userId, file);

        // Assert
        assertTrue(filename.startsWith("u" + userId + "-"));
        assertTrue(filename.endsWith(".jpg"));
        assertTrue(Files.exists(tempDir.resolve(filename)));
    }

    @Test
    void saveAvatar_fail_nullFile_shouldThrowBadRequest() {
        // Act
        AppException ex = assertThrows(AppException.class,
                () -> fileStorageService.saveAvatar(1L, null));

        // Assert
        assertEquals(ErrorCode.BAD_REQUEST, ex.getErrorCode());
    }

    @Test
    void saveAvatar_fail_emptyFile_shouldThrowBadRequest() {
        // Arrange
        MockMultipartFile empty = new MockMultipartFile(
                "avatar",
                "a.png",
                "image/png",
                new byte[0]
        );

        // Act
        AppException ex = assertThrows(AppException.class,
                () -> fileStorageService.saveAvatar(1L, empty));

        // Assert
        assertEquals(ErrorCode.BAD_REQUEST, ex.getErrorCode());
    }

    @Test
    void saveAvatar_fail_invalidContentType_shouldThrowBadRequest() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "a.gif",
                "image/gif",
                "gif".getBytes()
        );

        // Act
        AppException ex = assertThrows(AppException.class,
                () -> fileStorageService.saveAvatar(1L, file));

        // Assert
        assertEquals(ErrorCode.BAD_REQUEST, ex.getErrorCode());
    }

    @Test
    void saveAvatar_fail_nullContentType_shouldThrowBadRequest() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "a.bin",
                null,
                "x".getBytes()
        );

        // Act
        AppException ex = assertThrows(AppException.class,
                () -> fileStorageService.saveAvatar(1L, file));

        // Assert
        assertEquals(ErrorCode.BAD_REQUEST, ex.getErrorCode());
    }

    @Test
    void deleteAvatar_success_shouldDeleteIfExists() throws IOException {
        // Arrange: tạo sẵn file trong upload dir
        Path existing = tempDir.resolve("old.png");
        Files.write(existing, "x".getBytes());
        assertTrue(Files.exists(existing));

        // Act
        fileStorageService.deleteAvatar("old.png");

        // Assert
        assertFalse(Files.exists(existing));
    }

    @Test
    void deleteAvatar_nullOrBlank_shouldDoNothing_andNotThrow() {
        assertDoesNotThrow(() -> fileStorageService.deleteAvatar(null));
        assertDoesNotThrow(() -> fileStorageService.deleteAvatar(""));
        assertDoesNotThrow(() -> fileStorageService.deleteAvatar("   "));
    }
}