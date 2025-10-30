package nba.studix.fileservice.Controller;

import nba.studix.fileservice.DTO.FileUploadResponse;
import nba.studix.fileservice.Service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    private final FileStorageService fileStorageService;

    public FileUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload/submission")
    public ResponseEntity<?> uploadSubmissionFile(@RequestParam("file") MultipartFile file,
                                                  @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // TODO: Получить userId из токена (временная заглушка)
            Long userId = 3L; // Заглушка для student ID

            if (!fileStorageService.isValidFileType(file)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Неподдерживаемый тип файла. Разрешены: JPG, PNG, PDF, DOC, DOCX, TXT"
                ));
            }

            if (!fileStorageService.isValidFileSize(file)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Файл слишком большой. Максимальный размер: 5MB"
                ));
            }

            // Сохраняем файл
            String filePath = fileStorageService.storeFile(file, "assignments", userId, "submission");

            FileUploadResponse response = new FileUploadResponse();
            response.setFilePath(filePath);
            response.setFileName(file.getOriginalFilename());
            response.setFileSize(file.getSize());
            response.setMimeType(file.getContentType());
            response.setMessage("Файл успешно загружен");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error uploading submission file: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Ошибка при загрузке файла: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/upload/profile")
    public ResponseEntity<?> uploadProfilePhoto(@RequestParam("file") MultipartFile file,
                                                @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // TODO: Получить userId из токена (временная заглушка)
            Long userId = 1L; // Заглушка для user ID

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
            String filePath = fileStorageService.storeFile(file, "profiles", userId, "profile");

            FileUploadResponse response = new FileUploadResponse();
            response.setFilePath(filePath);
            response.setFileName(file.getOriginalFilename());
            response.setFileSize(file.getSize());
            response.setMimeType(file.getContentType());
            response.setMessage("Фото профиля успешно загружено");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error uploading profile photo: {}", e.getMessage());
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
            String originalFileName = fileStorageService.getOriginalFileName(decodedFilePath);

            // Определяем Content-Type
            String contentType = fileStorageService.determineContentType(decodedFilePath);

            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Content-Disposition", "attachment; filename=\"" + originalFileName + "\"")
                    .body(fileContent);

        } catch (Exception e) {
            logger.error("Error downloading file: {}", filePath, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Файл не найден: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/download/submission/{submissionId}")
    public ResponseEntity<?> downloadSubmissionFile(@PathVariable Long submissionId) {
        try {
            // TODO: Получить информацию о сдаче из submission-service
            // Временная заглушка
            String filePath = "assignments/submission_sample.pdf";
            String fileName = "submission.pdf";

            // Загружаем файл
            byte[] fileContent = fileStorageService.loadFile(filePath);

            // Определяем Content-Type
            String contentType = fileStorageService.determineContentType(filePath);

            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .body(fileContent);

        } catch (Exception e) {
            logger.error("Error downloading submission file: {}", submissionId, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Ошибка при скачивании файла: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/metadata/{filePath:.+}")
    public ResponseEntity<?> getFileMetadata(@PathVariable String filePath) {
        try {
            String decodedFilePath = URLDecoder.decode(filePath, StandardCharsets.UTF_8);
            var metadata = fileStorageService.getFileMetadata(decodedFilePath);

            if (metadata == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", metadata.getId());
            response.put("filePath", metadata.getFilePath());
            response.put("originalName", metadata.getOriginalName());
            response.put("fileSize", metadata.getFileSize());
            response.put("mimeType", metadata.getMimeType());
            response.put("uploadType", metadata.getUploadType());
            response.put("uploadedBy", metadata.getUploadedBy());
            response.put("uploadedAt", metadata.getUploadedAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting file metadata: {}", filePath, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Ошибка при получении метаданных: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserFiles(@PathVariable Long userId) {
        try {
            var files = fileStorageService.getUserFiles(userId);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            logger.error("Error getting user files: {}", userId, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Ошибка при получении файлов пользователя: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{filePath:.+}")
    public ResponseEntity<?> deleteFile(@PathVariable String filePath) {
        try {
            String decodedFilePath = URLDecoder.decode(filePath, StandardCharsets.UTF_8);
            boolean deleted = fileStorageService.deleteFile(decodedFilePath);

            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Файл успешно удален"));
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            logger.error("Error deleting file: {}", filePath, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Ошибка при удалении файла: " + e.getMessage()
            ));
        }
    }
}