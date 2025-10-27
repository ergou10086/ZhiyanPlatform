package hbnu.project.zhiyanauth.service;

import hbnu.project.zhiyanauth.model.dto.AvatarDTO;
import hbnu.project.zhiyancommonbasic.domain.R;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户头像服务接口
 *
 * @author ErgouTree
 */
public interface AvatarService {

    /**
     * 上传用户头像
     * 前端已完成裁剪，后端生成多尺寸缩略图
     *
     * @param userId 用户ID
     * @param file   图片文件（已裁剪）
     * @return 头像URL信息
     */
    R<AvatarDTO> uploadAvatar(Long userId, MultipartFile file);


    /**
     * 获取用户头像URL信息
     *
     * @param userId 用户ID
     * @return 头像URL信息
     */
    R<AvatarDTO> getAvatarInfo(Long userId);
}
