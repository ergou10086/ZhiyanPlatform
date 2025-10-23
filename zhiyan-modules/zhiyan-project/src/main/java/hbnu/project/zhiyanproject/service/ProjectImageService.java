package hbnu.project.zhiyanproject.service;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanproject.model.dto.ImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 项目图片服务接口
 *
 * @author Tokito
 */
public interface ProjectImageService {

    /**
     * 上传项目图片
     *
     * @param file 图片文件
     * @param projectId 项目ID（可选，用于生成路径）
     * @param userId 用户ID
     * @return 图片上传结果
     */
    R<ImageUploadResponse> uploadProjectImage(MultipartFile file, Long projectId, Long userId);

    /**
     * 删除项目图片
     *
     * @param imageUrl 图片URL
     * @return 删除结果
     */
    R<Void> deleteProjectImage(String imageUrl);
}

