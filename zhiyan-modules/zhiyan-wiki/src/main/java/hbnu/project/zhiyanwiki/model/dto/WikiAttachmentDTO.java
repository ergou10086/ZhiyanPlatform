package hbnu.project.zhiyanwiki.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Wiki附件DTO
 * 用于返回附件信息
 *
 * @author Tokito
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WikiAttachmentDTO {

    /**
     * 附件ID
     */
    private String id;

    /**
     * 所属Wiki页面ID
     */
    private String wikiPageId;

    /**
     * 所属项目ID
     */
    private String projectId;

    /**
     * 附件类型（IMAGE=图片, FILE=文件）
     */
    private String attachmentType;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件大小（格式化，如 1.5MB）
     */
    private String fileSizeFormatted;

    /**
     * 文件类型/扩展名
     */
    private String fileType;

    /**
     * 文件访问URL
     */
    private String fileUrl;

    /**
     * 文件描述
     */
    private String description;

    /**
     * 上传者ID
     */
    private String uploadBy;

    /**
     * 上传者用户名（可选，需要关联查询）
     */
    private String uploadByUsername;

    /**
     * 上传时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadAt;
}


