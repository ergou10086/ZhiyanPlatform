package hbnu.project.zhiyancommonoss.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件下载请求DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDownloadRequest {

    /**
     * 对象键（文件路径）
     */
    private String objectKey;

    /**
     * 是否生成预签名URL（默认false，直接下载）
     */
    private Boolean generatePresignedUrl;

    /**
     * 预签名URL过期时间（秒，默认3600）
     */
    private Integer expirySeconds;

    /**
     * 当前用户ID（用于权限验证）
     */
    private Long userId;

    /**
     * 用户所在项目ID列表（用于权限验证）
     */
    private java.util.List<Long> userProjectIds;
}
