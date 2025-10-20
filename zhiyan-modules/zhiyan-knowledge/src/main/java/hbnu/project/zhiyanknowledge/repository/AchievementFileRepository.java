package hbnu.project.zhiyanknowledge.repository;

import hbnu.project.zhiyanknowledge.model.entity.AchievementFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 成果文件数据访问层
 * 管理成果关联的文件信息，只是在这里进行简单的查询，es部分会处理的更详细
 *
 * @author ErgouTree
 */
@Repository
public interface AchievementFileRepository extends JpaRepository<AchievementFile, Long> {

    /**
     * 根据成果ID查询所有文件（分页）
     *
     * @param achievementId 成果ID
     * @param pageable      分页参数
     * @return 文件分页列表
     */
    Page<AchievementFile> findByAchievementId(Long achievementId, Pageable pageable);

    /**
     * 根据成果ID查询所有文件
     *
     * @param achievementId 成果ID
     * @return 文件列表
     */
    List<AchievementFile> findByAchievementId(Long achievementId);

//    /**
//     * 根据成果ID和文件名查询文件列表（包含所有版本）
//     *
//     * @param achievementId 成果ID
//     * @param fileName      文件名
//     * @return 文件列表（按版本降序）
//     */
//    @Query("SELECT f FROM AchievementFile f WHERE f.achievementId = :achievementId " +
//           "AND f.fileName = :fileName ORDER BY f.version DESC")
//    List<AchievementFile> findVersionsByAchievementIdAndFileName(@Param("achievementId") Long achievementId,
//                                                                  @Param("fileName") String fileName);

    /**
     * 根据成果ID和文件名查询文件
     * 比较少的使用，一般在es操作
     *
     * @param achievementId 成果ID
     * @param fileName      文件名
     * @return 最新版本文件
     */
    Optional<AchievementFile> findByAchievementIdAndFileNameAndIsLatestTrue(Long achievementId, String fileName);

    /**
     * 根据成果ID和版本号查询文件
     *
     * @param achievementId 成果ID
     * @param fileName      文件名
     * @param version       版本号
     * @return 文件
     */
    Optional<AchievementFile> findByAchievementIdAndFileNameAndVersion(Long achievementId,
                                                                        String fileName,
                                                                        Integer version);

    /**
     * 根据上传者ID查询文件列表（分页）
     *
     * @param uploadBy 上传者ID
     * @param pageable 分页参数
     * @return 文件分页列表
     */
    Page<AchievementFile> findByUploadBy(Long uploadBy, Pageable pageable);

    /**
     * 统计成果的文件数量
     *
     * @param achievementId 成果ID
     * @return 文件数量
     */
    long countByAchievementId(Long achievementId);

    /**
     * 删除该成果的所有文件
     *
     * @param achievementId 成果ID
     */
    void deleteByAchievementId(Long achievementId);

    /**
     * 根据MinIO对象键查询文件
     *
     * @param objectKey MinIO对象键
     * @return 文件
     */
    Optional<AchievementFile> findByObjectKey(String objectKey);

    /**
     * 根据桶名和对象键查询文件
     *
     * @param bucketName 桶名
     * @param objectKey  对象键
     * @return 文件
     */
    Optional<AchievementFile> findByBucketNameAndObjectKey(String bucketName, String objectKey);

    /**
     * 检查文件是否存在
     *
     * @param achievementId 成果ID
     * @param fileName      文件名
     * @return 是否存在
     */
    boolean existsByAchievementIdAndFileName(Long achievementId, String fileName);

    /**
     * 根据IDs，批量查询成果文件
     *
     * @param achievementIds 成果ID列表
     * @return 文件列表
     */
    @Query("SELECT f FROM AchievementFile f WHERE f.achievementId IN :achievementIds " +
           "AND f.isLatest = true")
    List<AchievementFile> findLatestFilesByAchievementIdIn(@Param("achievementIds") List<Long> achievementIds);

    /**
     * 根据文件类型查询文件（分页）
     * 预留，不一定用得到
     *
     * @param fileType 文件类型
     * @param pageable 分页参数
     * @return 文件分页列表
     */
    Page<AchievementFile> findByFileType(String fileType, Pageable pageable);

    /**
     * 统计指定类型的文件数量
     *
     * @param fileType 文件类型
     * @return 文件数量
     */
    long countByFileType(String fileType);
}

