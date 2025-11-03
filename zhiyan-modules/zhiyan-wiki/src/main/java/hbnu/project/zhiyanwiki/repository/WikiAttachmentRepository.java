package hbnu.project.zhiyanwiki.repository;

import hbnu.project.zhiyanwiki.model.entity.WikiAttachment;
import hbnu.project.zhiyanwiki.model.enums.AttachmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Wiki附件Repository
 * 提供附件的数据访问操作
 *
 * @author Tokito
 */
@Repository
public interface WikiAttachmentRepository extends JpaRepository<WikiAttachment, Long> {

    /**
     * 根据Wiki页面ID查询所有附件（不包括已删除）
     *
     * @param wikiPageId Wiki页面ID
     * @return 附件列表
     */
    List<WikiAttachment> findByWikiPageIdAndIsDeletedFalse(Long wikiPageId);

    /**
     * 根据Wiki页面ID和附件类型查询附件
     *
     * @param wikiPageId     Wiki页面ID
     * @param attachmentType 附件类型
     * @return 附件列表
     */
    List<WikiAttachment> findByWikiPageIdAndAttachmentTypeAndIsDeletedFalse(
            Long wikiPageId, AttachmentType attachmentType);

    /**
     * 根据项目ID查询所有附件（分页）
     *
     * @param projectId 项目ID
     * @param pageable  分页参数
     * @return 附件分页
     */
    Page<WikiAttachment> findByProjectIdAndIsDeletedFalse(Long projectId, Pageable pageable);

    /**
     * 根据项目ID和附件类型查询附件（分页）
     *
     * @param projectId      项目ID
     * @param attachmentType 附件类型
     * @param pageable       分页参数
     * @return 附件分页
     */
    Page<WikiAttachment> findByProjectIdAndAttachmentTypeAndIsDeletedFalse(
            Long projectId, AttachmentType attachmentType, Pageable pageable);

    /**
     * 根据ID查询附件（不包括已删除）
     *
     * @param id 附件ID
     * @return 附件Optional
     */
    Optional<WikiAttachment> findByIdAndIsDeletedFalse(Long id);

    /**
     * 根据上传者ID查询附件
     *
     * @param uploadBy 上传者ID
     * @param pageable 分页参数
     * @return 附件分页
     */
    Page<WikiAttachment> findByUploadByAndIsDeletedFalse(Long uploadBy, Pageable pageable);

    /**
     * 根据文件名模糊查询附件
     *
     * @param projectId 项目ID
     * @param fileName  文件名关键字
     * @param pageable  分页参数
     * @return 附件分页
     */
    Page<WikiAttachment> findByProjectIdAndFileNameContainingAndIsDeletedFalse(
            Long projectId, String fileName, Pageable pageable);

    /**
     * 统计Wiki页面的附件数量
     *
     * @param wikiPageId Wiki页面ID
     * @return 附件数量
     */
    long countByWikiPageIdAndIsDeletedFalse(Long wikiPageId);

    /**
     * 统计项目的附件总数
     *
     * @param projectId 项目ID
     * @return 附件数量
     */
    long countByProjectIdAndIsDeletedFalse(Long projectId);

    /**
     * 统计项目的附件总大小
     *
     * @param projectId 项目ID
     * @return 总大小（字节）
     */
    @Query("SELECT COALESCE(SUM(a.fileSize), 0) FROM WikiAttachment a " +
           "WHERE a.projectId = :projectId AND a.isDeleted = false")
    long sumFileSizeByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据对象键查询附件
     *
     * @param objectKey 对象键
     * @return 附件Optional
     */
    Optional<WikiAttachment> findByObjectKeyAndIsDeletedFalse(String objectKey);

    /**
     * 批量删除Wiki页面的附件（软删除）
     *
     * @param wikiPageId Wiki页面ID
     */
    @Query("UPDATE WikiAttachment a SET a.isDeleted = true, a.deletedAt = CURRENT_TIMESTAMP " +
           "WHERE a.wikiPageId = :wikiPageId AND a.isDeleted = false")
    void softDeleteByWikiPageId(@Param("wikiPageId") Long wikiPageId);

    /**
     * 获取项目最近上传的附件
     *
     * @param projectId 项目ID
     * @param pageable  分页参数
     * @return 附件列表
     */
    @Query("SELECT a FROM WikiAttachment a " +
           "WHERE a.projectId = :projectId AND a.isDeleted = false " +
           "ORDER BY a.uploadAt DESC")
    List<WikiAttachment> findRecentAttachments(@Param("projectId") Long projectId, Pageable pageable);
}


