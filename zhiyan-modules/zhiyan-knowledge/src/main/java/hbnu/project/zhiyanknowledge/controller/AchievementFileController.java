package hbnu.project.zhiyanknowledge.controller;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanknowledge.model.dto.AchievementFileDTO;
import hbnu.project.zhiyanknowledge.model.dto.UploadFileDTO;
import hbnu.project.zhiyanknowledge.service.AchievementFileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 成果文件上传接口
 * 负责把文件上传到成果中
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/zhiyan/achievement/file")
@Tag(name = "成果的文件管理", description = "成果文件上传等管理")
public class AchievementFileController {

    @Autowired
    private AchievementFileService achievementFileService;

    /**
     * 上传成果文件
     * 上传单个文件到指定成果
     */
    @PostMapping("/file/upload")
    @Operation(summary = "上传成果文件", description = "为指定成果上传单个文件")
    public R<AchievementFileDTO> uploadFile(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "成果ID") @RequestParam("achievementId") Long achievementId
    ){
        // 从安全上下文获取当前登录用户ID
        Long userId = SecurityUtils.getUserId();
        log.info("上传成果文件: achievementId={}, fileName={}, size={}, userId={}",
                achievementId, file.getOriginalFilename(), file.getSize(), userId);

        UploadFileDTO uploadDTO = UploadFileDTO.builder()
                .achievementId(achievementId)
                .uploadBy(userId)
                .build();

        AchievementFileDTO fileDTO = achievementFileService.uploadFile(file, uploadDTO);

        log.info("文件上传成功: fileId={}, url={}", fileDTO.getId(), fileDTO.getFileUrl());
        return R.ok(fileDTO, "文件上传成功");
    }


    /**
     * 批量上传成果文件
     * 批量上传多个文件到指定成果
     */
    @PostMapping("/file/upload/batch")
    @Operation(summary = "批量上传成果文件", description = "为指定成果批量上传多个文件")
    public R<List<AchievementFileDTO>> uploadFilesBatch(
            @Parameter(description = "文件列表") @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "成果ID") @RequestParam("achievementId") Long achievementId
    ){
        // 从安全上下文获取当前登录用户ID
        Long userId = SecurityUtils.getUserId();
        log.info("批量上传成果文件: achievementId={}, fileCount={}, userId={}", 
                achievementId, files.size(), userId);

        List<AchievementFileDTO> fileDTOs = achievementFileService.uploadFilesBatch(files, achievementId, userId);

        log.info("批量上传成功: fileCount={}", fileDTOs.size());
        return R.ok(fileDTOs, String.format("成功上传%d个文件", fileDTOs.size()));
    }


    /**
     * 查询成果的所有文件
     * 获取指定成果下的所有文件列表
     */
    @GetMapping("/{achievementId}/files")
    @Operation(summary = "查询成果文件列表", description = "获取指定成果下的所有文件")
    public R<List<AchievementFileDTO>> getAchievementFiles(
            @Parameter(description = "成果ID") @PathVariable Long achievementId) {

        log.info("查询成果文件列表: achievementId={}", achievementId);

        List<AchievementFileDTO> files = achievementFileService.getFilesByAchievementId(achievementId);

        return R.ok(files, "查询成功");
    }


    /**
     * 删除成果文件
     * 删除指定的文件
     */
    @DeleteMapping("/file/{fileId}")
    @Operation(summary = "删除成果文件", description = "删除指定的成果文件")
    public R<Void> deleteAchievementFile(
            @Parameter(description = "文件ID")  @PathVariable Long fileId
    ){
        // 从安全上下文获取当前登录用户ID
        Long userId = SecurityUtils.getUserId();
        log.info("删除成果文件: fileId={}, userId={}", fileId, userId);

        achievementFileService.deleteFile(fileId, userId);

        log.info("文件删除成功: fileId={}", fileId);
        return R.ok(null, "文件删除成功");
    }


    /**
     * 获取文件下载URL
     * 生成临时的文件下载链接
     */
    @GetMapping("/file/{fileId}/download-url")
    @Operation(summary = "获取文件下载URL", description = "生成文件的临时下载链接")
    public R<String> getFileDownloadUrl(
            @Parameter(description = "文件ID") @PathVariable Long fileId,
            @Parameter(description = "过期时间(秒)") @RequestParam(defaultValue = "3600") Integer expirySeconds
    ) {
        // 从安全上下文获取当前登录用户ID
        Long userId = SecurityUtils.getUserId();
        log.info("获取文件下载URL: fileId={}, expirySeconds={}", fileId, expirySeconds);

        String downloadUrl = achievementFileService.getFileDownloadUrl(fileId, userId, expirySeconds);

        return R.ok(downloadUrl, "下载链接生成成功");
    }

}
