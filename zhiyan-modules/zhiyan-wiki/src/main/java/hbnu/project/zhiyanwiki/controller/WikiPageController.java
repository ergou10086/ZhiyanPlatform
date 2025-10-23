package hbnu.project.zhiyanwiki.controller;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanwiki.model.dto.*;
import hbnu.project.zhiyanwiki.model.entity.WikiPage;
import hbnu.project.zhiyanwiki.service.WikiContentService;
import hbnu.project.zhiyanwiki.service.WikiPageService;
import hbnu.project.zhiyanwiki.utils.WikiSecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Wiki页面控制器
 * 提供Wiki页面的CRUD和权限控制
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequestMapping("/api/wiki")
@RequiredArgsConstructor
@Tag(name = "Wiki管理", description = "Wiki页面管理相关接口")
public class WikiPageController {

    @Autowired
    private final WikiPageService wikiPageService;

    @Autowired
    private final WikiContentService wikiContentService;

    @Autowired
    private final WikiSecurityUtils wikiSecurityUtils;

    /**
     * 创建Wiki页面
     * 权限要求：已登录 + 项目成员
     */
    @PostMapping("/pages")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "创建Wiki页面", description = "创建新的Wiki页面（目录或文档）")
    public R<WikiPage> createPage(@RequestBody @Valid CreateWikiPageDTO dto) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return R.fail(R.UNAUTHORIZED, "未登录或令牌无效");
        }

        log.info("用户[{}]创建Wiki页面: projectId={}, title={}", userId, dto.getProjectId(), dto.getTitle());

        // 权限检查：必须是项目成员
        wikiSecurityUtils.requireProjectMember(dto.getProjectId());

        dto.setCreatorId(userId);
        WikiPage page = wikiPageService.createWikiPage(dto);

        return R.ok(page, "Wiki页面创建成功");
    }

    /**
     * 更新Wiki页面
     * 权限要求：已登录 + 项目成员
     */
    @PutMapping("/pages/{pageId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "更新Wiki页面", description = "更新Wiki页面的标题和内容")
    public R<WikiPage> updatePage(
            @Parameter(description = "页面ID") @PathVariable Long pageId,
            @RequestBody @Valid UpdateWikiPageDTO dto) {

        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]更新Wiki页面[{}]", userId, pageId);

        // 权限检查：必须有编辑权限
        wikiSecurityUtils.requireEdit(pageId);

        dto.setEditorId(userId);
        WikiPage page = wikiPageService.updateWikiPage(
                pageId,
                dto.getTitle(),
                dto.getContent(),
                dto.getChangeDescription(),
                userId
        );

        return R.ok(page, "Wiki页面更新成功");
    }

    /**
     * 删除Wiki页面
     * 权限要求：已登录 + 有删除权限（创建者或管理员）
     */
    @DeleteMapping("/pages/{pageId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "删除Wiki页面", description = "删除指定Wiki页面")
    public R<Void> deletePage(@PathVariable Long pageId) {
        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]删除Wiki页面[{}]", userId, pageId);

        // 权限检查：必须有删除权限
        wikiSecurityUtils.requireDelete(pageId);

        wikiPageService.deleteWikiPage(pageId);

        return R.ok(null, "Wiki页面删除成功");
    }

    /**
     * 递归删除Wiki页面及其子页面
     * 权限要求：已登录 + 项目管理员
     */
    @DeleteMapping("/pages/{pageId}/recursive")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "递归删除Wiki页面", description = "删除Wiki页面及其所有子页面")
    public R<Void> deletePageRecursively(@PathVariable Long pageId) {
        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]递归删除Wiki页面[{}]", userId, pageId);

        // 权限检查：必须有删除权限
        wikiSecurityUtils.requireDelete(pageId);

        wikiPageService.deletePageRecursively(pageId);

        return R.ok(null, "Wiki页面及子页面删除成功");
    }

    /**
     * 获取Wiki页面详情（含内容）
     * 权限要求：已登录 + 有访问权限（项目成员或公开页面）
     */
    @GetMapping("/pages/{pageId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取Wiki页面详情", description = "获取Wiki页面的完整信息和内容")
    public R<WikiPageDetailDTO> getPageDetail(@PathVariable Long pageId) {
        Long userId = SecurityUtils.getUserId();
        log.debug("用户[{}]查询Wiki页面[{}]", userId, pageId);

        // 权限检查：必须有访问权限
        wikiSecurityUtils.requireAccess(pageId);

        WikiPageDetailDTO detail = wikiPageService.getWikiPageWithContent(pageId);

        return R.ok(detail);
    }

    /**
     * 获取项目的Wiki树状结构
     * 权限要求：已登录 + 项目成员
     */
    @GetMapping("/projects/{projectId}/tree")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取Wiki树", description = "获取项目的Wiki树状目录结构")
    public R<List<WikiPageTreeDTO>> getProjectWikiTree(@PathVariable Long projectId) {
        Long userId = SecurityUtils.getUserId();
        log.debug("用户[{}]查询项目[{}]的Wiki树", userId, projectId);

        // 权限检查：必须是项目成员
        wikiSecurityUtils.requireProjectMember(projectId);

        List<WikiPageTreeDTO> tree = wikiPageService.getProjectWikiTree(projectId);

        return R.ok(tree);
    }

    /**
     * 搜索Wiki页面（标题）
     * 权限要求：已登录 + 项目成员
     */
    @GetMapping("/projects/{projectId}/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "搜索Wiki页面", description = "根据关键字搜索项目中的Wiki页面")
    public R<Page<WikiSearchDTO>> searchWiki(
            @PathVariable Long projectId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = SecurityUtils.getUserId();
        log.debug("用户[{}]搜索项目[{}]的Wiki: keyword={}", userId, projectId, keyword);

        // 权限检查：必须是项目成员
        wikiSecurityUtils.requireProjectMember(projectId);

        Pageable pageable = PageRequest.of(page, size);
        Page<WikiSearchDTO> result = wikiPageService.searchByTitle(projectId, keyword, pageable);

        return R.ok(result);
    }

    /**
     * 全文搜索Wiki内容
     * 权限要求：已登录 + 项目成员
     */
    @GetMapping("/projects/{projectId}/search/content")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "全文搜索Wiki", description = "根据关键字搜索Wiki内容")
    public R<List<WikiSearchDTO>> searchContent(
            @PathVariable Long projectId,
            @RequestParam String keyword) {

        Long userId = SecurityUtils.getUserId();
        log.debug("用户[{}]全文搜索项目[{}]的Wiki: keyword={}", userId, projectId, keyword);

        // 权限检查：必须是项目成员
        wikiSecurityUtils.requireProjectMember(projectId);

        List<WikiSearchDTO> result = wikiPageService.searchByContent(projectId, keyword);

        return R.ok(result);
    }

    /**
     * 移动Wiki页面
     * 权限要求：已登录 + 有编辑权限
     */
    @PatchMapping("/pages/{pageId}/move")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "移动Wiki页面", description = "移动Wiki页面到新的父页面下")
    public R<Void> movePage(
            @PathVariable Long pageId,
            @RequestBody @Valid MoveWikiPageDTO dto) {

        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]移动Wiki页面[{}]到父页面[{}]", userId, pageId, dto.getNewParentId());

        // 权限检查：必须有编辑权限
        wikiSecurityUtils.requireEdit(pageId);

        wikiPageService.moveWikiPage(pageId, dto.getNewParentId());

        return R.ok(null, "Wiki页面移动成功");
    }


    /**
     * 获取Wiki版本历史
     * 权限要求：已登录 + 有访问权限
     */
    @GetMapping("/pages/{pageId}/versions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取版本历史", description = "获取Wiki页面的所有版本历史")
    public R<List<WikiVersionDTO>> getVersionHistory(@PathVariable Long pageId) {
        Long userId = SecurityUtils.getUserId();
        log.debug("用户[{}]查询Wiki页面[{}]的版本历史", userId, pageId);

        // 权限检查：必须有访问权限
        wikiSecurityUtils.requireAccess(pageId);

        List<WikiVersionDTO> versions = wikiContentService.getVersionHistory(pageId);

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
            @PathVariable Long pageId,
            @PathVariable Integer version) {

        Long userId = SecurityUtils.getUserId();
        log.debug("用户[{}]查询Wiki页面[{}]的版本[{}]", userId, pageId, version);

        // 权限检查：必须有访问权限
        wikiSecurityUtils.requireAccess(pageId);

        String content = wikiContentService.getVersionContent(pageId, version);

        return R.ok(content);
    }

    /**
     * 复制Wiki页面
     * 权限要求：已登录 + 项目成员
     */
    @PostMapping("/pages/{pageId}/copy")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "复制Wiki页面", description = "复制Wiki页面到指定位置")
    public R<WikiPage> copyPage(
            @PathVariable Long pageId,
            @RequestParam(required = false) Long targetParentId,
            @RequestParam(required = false) String newTitle) {

        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]复制Wiki页面[{}]", userId, pageId);

        // 权限检查：必须有访问权限和创建权限
        wikiSecurityUtils.requireAccess(pageId);

        WikiPage newPage = wikiPageService.copyPage(pageId, targetParentId, newTitle, userId);

        return R.ok(newPage, "Wiki页面复制成功");
    }

    /**
     * 获取项目Wiki统计信息
     * 权限要求：已登录 + 项目成员
     */
    @GetMapping("/projects/{projectId}/statistics")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取Wiki统计", description = "获取项目Wiki的统计信息")
    public R<WikiStatisticsDTO> getStatistics(@PathVariable Long projectId) {
        Long userId = SecurityUtils.getUserId();
        log.debug("用户[{}]查询项目[{}]的Wiki统计", userId, projectId);

        // 权限检查：必须是项目成员
        wikiSecurityUtils.requireProjectMember(projectId);

        WikiStatisticsDTO statistics = wikiPageService.getProjectStatistics(projectId);

        return R.ok(statistics);
    }

    /**
     * 获取最近更新的Wiki页面
     * 权限要求：已登录 + 项目成员
     */
    @GetMapping("/projects/{projectId}/recent")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取最近更新", description = "获取项目中最近更新的Wiki页面")
    public R<List<WikiPageTreeDTO>> getRecentlyUpdated(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "10") int limit) {

        Long userId = SecurityUtils.getUserId();
        log.debug("用户[{}]查询项目[{}]最近更新的Wiki", userId, projectId);

        // 权限检查：必须是项目成员
        wikiSecurityUtils.requireProjectMember(projectId);

        List<WikiPageTreeDTO> pages = wikiPageService.getRecentlyUpdated(projectId, limit);

        return R.ok(pages);
    }
}
