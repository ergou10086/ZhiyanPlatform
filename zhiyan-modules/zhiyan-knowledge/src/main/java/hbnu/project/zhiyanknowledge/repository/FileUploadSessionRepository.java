package hbnu.project.zhiyanknowledge.repository;

import hbnu.project.zhiyanknowledge.model.entity.FileUploadSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 文件上传会话repo
 *
 * @author ErgouTree
 */
@Repository
public interface FileUploadSessionRepository extends JpaRepository<FileUploadSession,Long> {

    /**
     * 根据uploadId查询会话
     */
    Optional<FileUploadSession> findByUploadId(String uploadId);


    /**
     * 查询用户在指定成果下的所有进行中会话
     */
    List<FileUploadSession> findByAchievementIdAndUploadByAndStatus(Long achievementId, Long uploadBy, FileUploadSession.UploadStatus status);

    /**
     * 清理过期的会话
     */
    @Modifying
    @Query("DELETE FROM FileUploadSession f WHERE f.expiredAt < ?1 AND f.status <> 'COMPLETED'")
    int deleteExpiredSessions(LocalDateTime now);

    /**
     * 查询用户的所有进行中会话
     */
    List<FileUploadSession> findByUploadByAndStatus(Long uploadBy, FileUploadSession.UploadStatus status);
}
