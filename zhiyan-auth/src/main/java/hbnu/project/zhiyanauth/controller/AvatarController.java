package hbnu.project.zhiyanauth.controller;

import hbnu.project.zhiyanauth.model.dto.AvatarDTO;
import hbnu.project.zhiyanauth.service.AvatarService;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.exception.ControllerException;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
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
                log.warn("上传头像失败: 用户未登录");
                return R.fail("用户未登录");
            }

            // 调用服务层上传头像
            R<AvatarDTO> result = avatarService.uploadAvatar(userId, file);
            log.info("上传头像结果: code={}, msg={}, hasData={}", result.getCode(), result.getMsg(), result.getData() != null);
            if (result.getData() != null) {
                log.info("头像响应数据 - minioUrl: {}, cdnUrl: {}", 
                    result.getData().getMinioUrl(), result.getData().getCdnUrl());
            }
            return result;
        }catch (ControllerException e){
            log.error("上传头像失败 - ControllerException", e);
            return R.fail("上传头像失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("上传头像异常 - Exception", e);
            return R.fail("上传头像异常: " + e.getMessage());
        }
    }


    /**
     * 获取当前用户头像信息
     * 路径: GET /api/users/avatar/me_avatar
     * 权限: 所有已登录用户
     */
    @GetMapping("/me_avatar")
    @Operation(summary = "获取头像信息", description = "获取当前用户的头像URL信息（包含所有尺寸）")
    public R<AvatarDTO> getMyAvatar() {
        log.info("获取当前用户头像信息");

        try {
            Long userId = SecurityUtils.getUserId();
            if (userId == null) {
                return R.fail("用户未登录");
            }

            return avatarService.getAvatarInfo(userId);

        } catch (Exception e) {
            log.error("获取头像信息失败", e);
            return R.fail("获取头像信息失败");
        }
    }



    /**
     * 根据用户ID获取头像信息（内部接口,服务间调用预留）
     * 路径: GET /api/users/avatar/{userId}
     * 用于其他服务查询用户头像
     */
    @GetMapping("/{userId}")
    @Operation(summary = "获取指定用户头像", description = "根据用户ID获取头像信息（服务间调用）")
    public R<AvatarDTO> getAvatar(@Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        log.info("获取用户头像信息: userId={}", userId);

        try {
            return avatarService.getAvatarInfo(userId);
        } catch (Exception e) {
            log.error("获取用户头像信息失败: userId={}", userId, e);
            return R.fail("获取头像信息失败");
        }
    }
}
