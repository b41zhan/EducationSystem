package nba.studix.fileservice.Repository;

import nba.studix.fileservice.Entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    Optional<FileMetadata> findByFilePath(String filePath);

    @Query("SELECT fm FROM FileMetadata fm WHERE fm.uploadedBy = :uploadedBy AND fm.isDeleted = false")
    List<FileMetadata> findByUploadedBy(@Param("uploadedBy") Long uploadedBy);

    @Query("SELECT fm FROM FileMetadata fm WHERE fm.uploadType = :uploadType AND fm.isDeleted = false")
    List<FileMetadata> findByUploadType(@Param("uploadType") String uploadType);

    @Query("SELECT fm FROM FileMetadata fm WHERE fm.uploadedBy = :uploadedBy AND fm.uploadType = :uploadType AND fm.isDeleted = false")
    List<FileMetadata> findByUploadedByAndUploadType(@Param("uploadedBy") Long uploadedBy,
                                                     @Param("uploadType") String uploadType);

    @Query("SELECT COUNT(fm) FROM FileMetadata fm WHERE fm.uploadedBy = :uploadedBy AND fm.isDeleted = false")
    Long countByUploadedBy(@Param("uploadedBy") Long uploadedBy);
}