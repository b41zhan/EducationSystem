package nba.studix.submissionservice.DTO;

public class CreateSubmissionDTO {
    private Long assignmentId;
    private String filePath;
    private String fileName;
    private Long fileSize;
    private String comment;

    // Конструкторы
    public CreateSubmissionDTO() {}

    // Геттеры и сеттеры
    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
