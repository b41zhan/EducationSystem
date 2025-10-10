package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.Service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin("*")
public class FileUploadController {

    private final FileStorageService fileStorageService;
    public FileUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload/submission")
    public ResponseEntity<?> uploadSubmissionFile(@RequestParam("file") MultipartFile file) {
        try {
            if (!fileStorageService.isValidFileType(file)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Неподдерживаемый тип файла. Разрешены: JPG, PNG, PDF, DOC, DOCX"
                ));
            }

            if (!fileStorageService.isValidFileSize(file)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Файл слишком большой. Максимальный размер: 5MB"
                ));
            }

            // Сохраняем файл
            String filePath = fileStorageService.storeFile(file, "assignments");

            Map<String, Object> response = new HashMap<>();
            response.put("filePath", filePath);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", file.getSize());
            response.put("message", "Файл успешно загружен");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Ошибка при загрузке файла: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/upload/profile")
    public ResponseEntity<?> uploadProfilePhoto(@RequestParam("file") MultipartFile file) {
        try {
            // Проверяем что это изображение
            if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Разрешены только изображения (JPG, PNG)"
                ));
            }

            // Проверяем размер файла
            if (!fileStorageService.isValidFileSize(file)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Изображение слишком большое. Максимальный размер: 5MB"
                ));
            }

            // Сохраняем файл
            String filePath = fileStorageService.storeFile(file, "profile");

            Map<String, Object> response = new HashMap<>();
            response.put("filePath", filePath);
            response.put("fileName", file.getOriginalFilename());
            response.put("message", "Фото профиля успешно загружено");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Ошибка при загрузке фото: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/download/{filePath:.+}")
    public ResponseEntity<?> downloadFile(@PathVariable String filePath) {
        try {
            byte[] fileContent = fileStorageService.loadFile(filePath);

            // Определяем Content-Type
            String contentType = "application/octet-stream";
            if (filePath.endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (filePath.endsWith(".png")) {
                contentType = "image/png";
            } else if (filePath.endsWith(".doc")) {
                contentType = "application/msword";
            } else if (filePath.endsWith(".docx")) {
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }

            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Content-Disposition", "inline; filename=\"" + filePath + "\"")
                    .body(fileContent);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Файл не найден: " + e.getMessage()
            ));
        }
    }
}