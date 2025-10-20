package hbnu.project.zhiyancommonoss.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传请求DTO
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadRequest {

    /**
     * 项目ID（成果文件和Wiki资源必需）
     */
    private Long projectId;

    /**
     * 成果ID（成果文件必需）
     */
    private Long achievementId;

    /**
     * 文档ID（Wiki资源必需）
     */
    private String documentId;

    /**
     * 文件访问URL
     */
    private String url;

    /**
     * 文件名
     */
    private String filename;

    /**
     * 文件ETag
     */
    private String eTag;

//    /**
//     * 版本号
//     */
//    private Integer version;

    /**
     * 资源类型（Wiki资源必需: images/attachments）
     */
    private String resourceType;

    /**
     * 用户ID（临时文件必需）
     */
    private Long userId;

    /**
     * 备注信息
     */
    private String remark;
}
