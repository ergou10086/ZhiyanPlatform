package hbnu.project.zhiyanwiki.controller;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanwiki.model.dto.WikiExportDTO;
import hbnu.project.zhiyanwiki.model.dto.WikiImportDTO;
import hbnu.project.zhiyanwiki.model.dto.WikiImportResultDTO;
import hbnu.project.zhiyanwiki.service.WikiExportService;
import hbnu.project.zhiyanwiki.service.WikiImportService;
import hbnu.project.zhiyanwiki.utils.WikiSecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Wiki导入导出控制器
 * 提供Wiki页面的导入导出功能
 *
 * @author Tokito
 */
@Slf4j
@RestController
@RequestMapping("/api/wiki")
@RequiredArgsConstructor
@Tag(name = "Wiki导入导出", description = "Wiki页面导入导出相关接口")
public class WikiImportExportController {

    private final WikiExportService exportService;
    private final WikiImportService importService;
    private final WikiSecurityUtils wikiSecurityUtils;

    /**
     * 导出单个Wiki页面
     * 权限要求：已登录 + 有访问权限
     */
    @GetMapping("/pages/{pageId}/export")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "导出Wiki页面", description = "将Wiki页面导出为指定格式（Markdown/PDF/Word）")
    public void exportPage(
            @PathVariable Long pageId,
            @RequestParam(defaultValue = "MARKDOWN") String format,
            @RequestParam(defaultValue = "false") Boolean includeChildren,
            @RequestParam(defaultValue = "false") Boolean includeAttachments,
            HttpServletResponse response) throws IOException {

        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]导出Wiki页面[{}]，格式: {}", userId, pageId, format);

        // 权限检查：必须有访问权限
        wikiSecurityUtils.requireAccess(pageId);

        // 构建导出配置
        WikiExportDTO exportDTO = WikiExportDTO.builder()
                .format(format)
                .includeChildren(includeChildren)
                .includeAttachments(includeAttachments)
                .build();

        // 导出页面
        byte[] content = exportService.exportPage(pageId, exportDTO);

        // 设置响应头
        String fileName = "wiki_page_" + pageId + getFileExtension(format);
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        response.setContentType(getContentType(format));
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
        response.setContentLength(content.length);

        // 写入响应
        response.getOutputStream().write(content);
        response.flushBuffer();

        log.info("Wiki页面导出成功: pageId={}, format={}, size={}", pageId, format, content.length);
    }

    /**
     * 批量导出Wiki页面（打包为ZIP）
     * 权限要求：已登录 + 项目成员
     */
    @PostMapping("/projects/{projectId}/export/batch")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "批量导出Wiki页面", description = "将多个Wiki页面打包导出为ZIP文件")
    public void exportPages(
            @PathVariable Long projectId,
            @RequestBody List<Long> pageIds,
            @RequestParam(defaultValue = "MARKDOWN") String format,
            HttpServletResponse response) throws IOException {

        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]批量导出项目[{}]的Wiki页面，数量: {}", userId, projectId, pageIds.size());

        // 权限检查：必须是项目成员
        wikiSecurityUtils.requireProjectMember(projectId);

        // 构建导出配置
        WikiExportDTO exportDTO = WikiExportDTO.builder()
                .format(format)
                .pageIds(pageIds)
                .build();

        // 批量导出
        byte[] zipContent = exportService.exportPages(pageIds, exportDTO);

        // 设置响应头
        String fileName = "wiki_pages_" + projectId + ".zip";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        response.setContentType("application/zip");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
        response.setContentLength(zipContent.length);

        // 写入响应
        response.getOutputStream().write(zipContent);
        response.flushBuffer();

        log.info("批量导出成功: projectId={}, count={}, size={}", projectId, pageIds.size(), zipContent.length);
    }

    /**
     * 导出整个Wiki目录树
     * 权限要求：已登录 + 有访问权限
     */
    @GetMapping("/pages/{pageId}/export/directory")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "导出Wiki目录树", description = "导出Wiki目录及其所有子页面（ZIP格式）")
    public void exportDirectory(
            @PathVariable Long pageId,
            @RequestParam(defaultValue = "MARKDOWN") String format,
            HttpServletResponse response) throws IOException {

        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]导出Wiki目录树[{}]", userId, pageId);

        // 权限检查：必须有访问权限
        wikiSecurityUtils.requireAccess(pageId);

        // 构建导出配置
        WikiExportDTO exportDTO = WikiExportDTO.builder()
                .format(format)
                .includeChildren(true)
                .build();

        // 导出目录树
        byte[] zipContent = exportService.exportDirectory(pageId, exportDTO);

        // 设置响应头
        String fileName = "wiki_directory_" + pageId + ".zip";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        response.setContentType("application/zip");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
        response.setContentLength(zipContent.length);

        // 写入响应
        response.getOutputStream().write(zipContent);
        response.flushBuffer();

        log.info("目录树导出成功: pageId={}, size={}", pageId, zipContent.length);
    }

    /**
     * 导入Markdown文件
     * 权限要求：已登录 + 项目成员
     */
    @PostMapping("/projects/{projectId}/import")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "导入Markdown文件", description = "从Markdown文件创建Wiki页面")
    public R<WikiImportResultDTO> importMarkdown(
            @PathVariable Long projectId,
            @Parameter(description = "Markdown文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "父页面ID（可选）") @RequestParam(required = false) Long parentId,
            @Parameter(description = "是否覆盖同名页面") @RequestParam(defaultValue = "false") Boolean overwrite,
            @Parameter(description = "是否设为公开") @RequestParam(defaultValue = "false") Boolean isPublic) {

        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return R.fail(R.UNAUTHORIZED, "未登录或令牌无效");
        }

        log.info("用户[{}]导入Markdown到项目[{}]，文件: {}", userId, projectId, file.getOriginalFilename());

        // 权限检查：必须是项目成员
        wikiSecurityUtils.requireProjectMember(projectId);

        // 构建导入配置
        WikiImportDTO importDTO = WikiImportDTO.builder()
                .projectId(projectId)
                .parentId(parentId)
                .format("MARKDOWN")
                .overwrite(overwrite)
                .isPublic(isPublic)
                .importBy(userId)
                .build();

        // 执行导入
        WikiImportResultDTO result = importService.importFromMarkdown(file, importDTO);

        if (result.getSuccess()) {
            return R.ok(result, "导入成功");
        } else {
            return R.fail(result.getMessage(), result);
        }
    }

    /**
     * 批量导入Markdown文件
     * 权限要求：已登录 + 项目成员
     */
    @PostMapping("/projects/{projectId}/import/batch")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "批量导入Markdown文件", description = "一次导入多个Markdown文件")
    public R<WikiImportResultDTO> importMultipleMarkdown(
            @PathVariable Long projectId,
            @Parameter(description = "Markdown文件列表") @RequestParam("files") MultipartFile[] files,
            @Parameter(description = "父页面ID（可选）") @RequestParam(required = false) Long parentId,
            @Parameter(description = "是否覆盖同名页面") @RequestParam(defaultValue = "false") Boolean overwrite,
            @Parameter(description = "是否设为公开") @RequestParam(defaultValue = "false") Boolean isPublic) {

        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return R.fail(R.UNAUTHORIZED, "未登录或令牌无效");
        }

        log.info("用户[{}]批量导入Markdown到项目[{}]，文件数: {}", userId, projectId, files.length);

        // 权限检查：必须是项目成员
        wikiSecurityUtils.requireProjectMember(projectId);

        // 构建导入配置
        WikiImportDTO importDTO = WikiImportDTO.builder()
                .projectId(projectId)
                .parentId(parentId)
                .format("MARKDOWN")
                .overwrite(overwrite)
                .isPublic(isPublic)
                .importBy(userId)
                .build();

        // 执行批量导入
        WikiImportResultDTO result = importService.importMultipleMarkdown(files, importDTO);

        return R.ok(result, result.getMessage());
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String format) {
        return switch (format.toUpperCase()) {
            case "MARKDOWN", "MD" -> ".md";
            case "PDF" -> ".pdf";
            case "WORD", "DOCX" -> ".docx";
            default -> ".txt";
        };
    }

    /**
     * 获取Content-Type
     */
    private String getContentType(String format) {
        return switch (format.toUpperCase()) {
            case "MARKDOWN", "MD" -> "text/markdown; charset=UTF-8";
            case "PDF" -> "application/pdf";
            case "WORD", "DOCX" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }
}

