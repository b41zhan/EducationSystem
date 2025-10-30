package nba.studix.fileservice.DTO;

public class FileUploadResponse {
    private String filePath;
    private String fileName;
    private Long fileSize;
    private String message;
    private String mimeType;

    // Конструкторы
    public FileUploadResponse() {}

    public FileUploadResponse(String filePath, String fileName, Long fileSize, String message, String mimeType) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.message = message;
        this.mimeType = mimeType;
    }

    // Геттеры и сеттеры
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
}