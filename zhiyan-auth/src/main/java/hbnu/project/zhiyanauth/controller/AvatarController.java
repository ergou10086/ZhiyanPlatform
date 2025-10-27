package hbnu.project.zhiyanauth.controller;

import hbnu.project.zhiyanauth.model.dto.AvatarDTO;
import hbnu.project.zhiyanauth.service.AvatarService;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户头像管理控制器
 *
 * @author ErgouTree
 */
@RestController
@RequestMapping("/zhiyan/users/avatar")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户头像管理", description = "用户头像上传、更新、删除相关接口")
public class AvatarController {

    private final AvatarService avatarService;

    /**
     * 上传用户头像
     * 前端处理裁剪为正方形
     * 路径: POST /api/users/avatar/upload
     * 权限: 所有已登录用户（只能上传自己的头像）
     */
    @PostMapping("/upload")
    @Operation(summary = "上传头像", description = "上传用户头像")
    public R<AvatarDTO> uploadAvatar(
            @Parameter(description = "头像图片文件", required = true) @RequestParam("file") MultipartFile file
    ) {
        log.info("上传用户头像: filename={}, size={}", file.getOriginalFilename(), file.getSize());

        try{
            // 获取用户ID
            Long userId = SecurityUtils.getUserId();
            if (userId == null) {
                return R.fail("用户未登录");
            }

            // 调用服务层上传头像
            return avatarService.uploadAvatar(userId, file);
        }catch (Exception e){

        }
    }

}
