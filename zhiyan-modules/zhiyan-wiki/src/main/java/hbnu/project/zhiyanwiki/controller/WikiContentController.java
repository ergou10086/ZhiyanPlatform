package hbnu.project.zhiyanwiki.controller;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanwiki.model.dto.WikiVersionDTO;
import hbnu.project.zhiyanwiki.service.WikiContentService;
import hbnu.project.zhiyanwiki.utils.WikiSecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Wiki内容控制器
 * 提供Wiki内容版本管理的相关接口
 *
 * @author Tokito
 */
@Slf4j
@RestController
@RequestMapping("/api/wiki/content")
@RequiredArgsConstructor
@Tag(name = "Wiki内容管理", description = "Wiki内容版本管理相关接口")
public class WikiContentController {

    private final WikiContentService contentService;
    private final WikiSecurityUtils wikiSecurityUtils;

    /**
     * 获取Wiki页面的版本历史列表
     * 权限要求：已登录 + 有访问权限
     */
    @GetMapping("/pages/{pageId}/versions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取版本历史", description = "获取Wiki页面的所有版本历史记录")
    public R<List<WikiVersionDTO>> getVersionHistory(@PathVariable Long pageId) {
        Long userId = SecurityUtils.getUserId();
        log.debug("用户[{}]查询Wiki页面[{}]的版本历史", userId, pageId);

        // 权限检查：必须有访问权限
        wikiSecurityUtils.requireAccess(pageId);

        List<WikiVersionDTO> versions = contentService.getVersionHistory(pageId);

        return R.ok(versions);
    }

    /**
     * 获取指定版本的内容
     * 权限要求：已登录 + 有访问权限
     */
    @GetMapping("/pages/{pageId}/versions/{version}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取指定版本内容", description = "获取Wiki页面指定版本的完整内容")
    public R<String> getVersionContent(
            @Parameter(description = "页面ID") @PathVariable Long pageId,
            @Parameter(description = "版本号") @PathVariable Integer version) {

        Long userId = SecurityUtils.getUserId();
        log.debug("用户[{}]查询Wiki页面[{}]的版本[{}]", userId, pageId, version);

        // 权限检查：必须有访问权限
        wikiSecurityUtils.requireAccess(pageId);

        String content = contentService.getVersionContent(pageId, version);

        return R.ok(content);
    }

    /**
     * 比较两个版本之间的差异
     * 权限要求：已登录 + 有访问权限
     */
    @GetMapping("/pages/{pageId}/compare")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "比较版本差异", description = "比较Wiki页面两个版本之间的内容差异")
    public R<String> compareVersions(
            @Parameter(description = "页面ID") @PathVariable Long pageId,
            @Parameter(description = "版本1") @RequestParam Integer version1,
            @Parameter(description = "版本2") @RequestParam Integer version2) {

        Long userId = SecurityUtils.getUserId();
        log.debug("用户[{}]比较Wiki页面[{}]的版本差异: v{} vs v{}", userId, pageId, version1, version2);

        // 权限检查：必须有访问权限
        wikiSecurityUtils.requireAccess(pageId);

        String diff = contentService.getVersionDiff(pageId, version1, version2);

        return R.ok(diff);
    }

    /**
     * 获取Wiki页面当前内容
     * 权限要求：已登录 + 有访问权限
     */
    @GetMapping("/pages/{pageId}/current")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取当前内容", description = "获取Wiki页面当前版本的内容")
    public R<String> getCurrentContent(@PathVariable Long pageId) {
        Long userId = SecurityUtils.getUserId();
        log.debug("用户[{}]查询Wiki页面[{}]的当前内容", userId, pageId);

        // 权限检查：必须有访问权限
        wikiSecurityUtils.requireAccess(pageId);

        var content = contentService.getContent(pageId);

        return R.ok(content.getContent());
    }

    /**
     * 获取最近的版本历史（最多10个）
     * 权限要求：已登录 + 有访问权限
     */
    @GetMapping("/pages/{pageId}/versions/recent")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取最近版本", description = "获取Wiki页面最近的版本历史（最多10个）")
    public R<List<Object>> getRecentVersions(@PathVariable Long pageId) {
        Long userId = SecurityUtils.getUserId();
        log.debug("用户[{}]查询Wiki页面[{}]的最近版本", userId, pageId);

        // 权限检查：必须有访问权限
        wikiSecurityUtils.requireAccess(pageId);

        var versions = contentService.getRecentVersions(pageId);

        return R.ok(List.of(versions.toArray()));
    }

    /**
     * 获取所有版本历史（包括归档的）
     * 权限要求：已登录 + 有访问权限
     */
    @GetMapping("/pages/{pageId}/versions/all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取所有版本", description = "获取Wiki页面的所有版本历史（包括已归档的）")
    public R<List<Object>> getAllVersionHistory(@PathVariable Long pageId) {
        Long userId = SecurityUtils.getUserId();
        log.debug("用户[{}]查询Wiki页面[{}]的所有版本历史", userId, pageId);

        // 权限检查：必须有访问权限
        wikiSecurityUtils.requireAccess(pageId);

        List<Object> versions = contentService.getAllVersionHistory(pageId);

        return R.ok(versions);
    }
}
