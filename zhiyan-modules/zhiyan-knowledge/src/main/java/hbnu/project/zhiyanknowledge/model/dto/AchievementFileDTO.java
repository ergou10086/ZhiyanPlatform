package hbnu.project.zhiyanknowledge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 成果文件DTO
 * 用于返回文件信息
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementFileDTO {

    /**
     * 文件ID
     */
    private String id;

    /**
     * 所属成果ID
     */
    private String achievementId;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件大小（格式化后，如 "10.5 MB"）
     */
    private String fileSizeFormatted;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * MinIO桶名
     */
    private String bucketName;

    /**
     * MinIO对象键
     */
    private String objectKey;

    /**
     * 文件访问URL（可能是临时URL）
     */
    private String fileUrl;

//    /**
//     * 文件版本号
//     */
//    private Integer version;
//
//    /**
//     * 是否最新版本
//     */
//    private Boolean isLatest;

    /**
     * 上传者ID
     */
    private String uploadBy;

    /**
     * 上传者名称（需从auth服务获取）
     */
    private String uploaderName;

    /**
     * 上传时间
     */
    private LocalDateTime uploadAt;
}

