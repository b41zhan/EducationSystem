package com.springdemo.educationsystem.Controller;

import com.springdemo.educationsystem.Entity.Submission;
import com.springdemo.educationsystem.Repository.SubmissionRepository;
import com.springdemo.educationsystem.Service.FileStorageService;
import com.springdemo.educationsystem.Service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin("*")
public class FileUploadController {

    private final FileStorageService fileStorageService;
    private final SubmissionRepository submissionRepository;
    public FileUploadController(FileStorageService fileStorageService, SubmissionRepository submissionRepository) {
        this.fileStorageService = fileStorageService;
        this.submissionRepository = submissionRepository;
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
            // Декодируем путь, если он содержит спецсимволы
            String decodedFilePath = URLDecoder.decode(filePath, StandardCharsets.UTF_8);

            // Загружаем файл
            byte[] fileContent = fileStorageService.loadFile(decodedFilePath);

            // Получаем оригинальное имя файла из пути
            String originalFileName = getOriginalFileName(decodedFilePath);

            // Определяем Content-Type
            String contentType = determineContentType(decodedFilePath);

            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Content-Disposition", "attachment; filename=\"" + originalFileName + "\"")
                    .body(fileContent);

        } catch (Exception e) {
            System.out.println("Ошибка скачивания файла: " + filePath);
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Файл не найден: " + e.getMessage()
            ));
        }
    }

    private String getOriginalFileName(String filePath) {
        // Извлекаем оригинальное имя файла после UUID
        if (filePath.contains("_")) {
            return filePath.substring(filePath.lastIndexOf("_") + 1);
        }
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    private String determineContentType(String filePath) {
        String extension = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();

        switch (extension) {
            case "pdf": return "application/pdf";
            case "jpg": case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "txt": return "text/plain";
            default: return "application/octet-stream";
        }
    }

    @GetMapping("/download/submission/{submissionId}")
    public ResponseEntity<?> downloadSubmissionFile(@PathVariable Long submissionId) {
        try {
            // Получаем информацию о сдаче из базы данных
            Submission submission = submissionRepository.findById(submissionId)
                    .orElseThrow(() -> new RuntimeException("Submission not found"));

            String filePath = submission.getFilePath();
            if (filePath == null || filePath.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Файл не найден"));
            }

            // Загружаем файл
            byte[] fileContent = fileStorageService.loadFile(filePath);

            // Получаем оригинальное имя файла
            String originalFileName = submission.getFileName();
            if (originalFileName == null || originalFileName.isEmpty()) {
                originalFileName = getOriginalFileName(filePath);
            }

            // Определяем Content-Type
            String contentType = determineContentType(filePath);

            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Content-Disposition", "attachment; filename=\"" + originalFileName + "\"")
                    .body(fileContent);

        } catch (Exception e) {
            System.out.println("Ошибка скачивания файла submission: " + submissionId);
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Ошибка при скачивании файла: " + e.getMessage()
            ));
        }
    }
}