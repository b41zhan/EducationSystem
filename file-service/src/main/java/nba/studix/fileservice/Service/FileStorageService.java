package nba.studix.fileservice.Service;

import nba.studix.fileservice.Entity.FileMetadata;
import nba.studix.fileservice.Repository.FileMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final FileMetadataRepository fileMetadataRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public FileStorageService(FileMetadataRepository fileMetadataRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
    }

    public String storeFile(MultipartFile file, String subDirectory, Long uploadedBy, String uploadType) throws IOException {
        Path uploadPath = Paths.get(uploadDir, subDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String relativePath = subDirectory + "/" + fileName;

        // Сохраняем метаданные файла
        FileMetadata metadata = new FileMetadata();
        metadata.setFilePath(relativePath);
        metadata.setOriginalName(file.getOriginalFilename());
        metadata.setFileSize(file.getSize());
        metadata.setMimeType(file.getContentType());
        metadata.setUploadType(uploadType);
        metadata.setUploadedBy(uploadedBy);

        fileMetadataRepository.save(metadata);

        logger.info("File stored successfully: {}, size: {}, uploaded by: {}",
                relativePath, file.getSize(), uploadedBy);

        return relativePath;
    }

    public byte[] loadFile(String filePath) throws IOException {
        Path path = Paths.get(uploadDir, filePath);
        return Files.readAllBytes(path);
    }

    public boolean deleteFile(String filePath) throws IOException {
        Path path = Paths.get(uploadDir, filePath);
        boolean deleted = Files.deleteIfExists(path);

        if (deleted) {
            // Помечаем файл как удаленный в базе данных
            fileMetadataRepository.findByFilePath(filePath).ifPresent(metadata -> {
                metadata.setIsDeleted(true);
                fileMetadataRepository.save(metadata);
                logger.info("File marked as deleted: {}", filePath);
            });
        }

        return deleted;
    }

    public boolean isValidFileType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null &&
                (contentType.startsWith("image/") ||
                        contentType.equals("application/pdf") ||
                        contentType.equals("application/msword") ||
                        contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                        contentType.equals("text/plain"));
    }

    public boolean isValidFileSize(MultipartFile file) {
        return file.getSize() <= 5 * 1024 * 1024; // 5MB
    }

    public String getOriginalFileName(String filePath) {
        if (filePath.contains("_")) {
            return filePath.substring(filePath.lastIndexOf("_") + 1);
        }
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    public String determineContentType(String filePath) {
        String extension = "";
        if (filePath.contains(".")) {
            extension = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();
        }

        switch (extension) {
            case "pdf": return "application/pdf";
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "txt": return "text/plain";
            default: return "application/octet-stream";
        }
    }

    public FileMetadata getFileMetadata(String filePath) {
        return fileMetadataRepository.findByFilePath(filePath).orElse(null);
    }

    public java.util.List<FileMetadata> getUserFiles(Long userId) {
        return fileMetadataRepository.findByUploadedBy(userId);
    }

    public java.util.List<FileMetadata> getFilesByType(String uploadType) {
        return fileMetadataRepository.findByUploadType(uploadType);
    }
}
