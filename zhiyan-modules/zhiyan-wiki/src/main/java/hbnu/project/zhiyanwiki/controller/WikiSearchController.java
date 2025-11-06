package hbnu.project.zhiyanwiki.controller;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanwiki.model.dto.WikiSearchResultDTO;
import hbnu.project.zhiyanwiki.service.WikiSearchService;
import hbnu.project.zhiyanwiki.utils.WikiSecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Wiki全文搜索控制器
 * 提供基于MongoDB文本索引的高性能搜索功能
 *
 * @author Tokito
 */
@Slf4j
@RestController
@RequestMapping("/api/wiki/search")
@RequiredArgsConstructor
@Tag(name = "Wiki搜索", description = "Wiki全文搜索相关接口")
public class WikiSearchController {

    private final WikiSearchService searchService;
    private final WikiSecurityUtils wikiSecurityUtils;

    /**
     * 全文搜索Wiki内容（支持分页和评分）
     * 权限要求：已登录 + 项目成员
     */
    @GetMapping("/projects/{projectId}/fulltext")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "全文搜索Wiki", description = "基于MongoDB文本索引的全文搜索，支持相关性评分和关键字高亮")
    public R<Page<WikiSearchResultDTO>> fullTextSearch(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @Parameter(description = "搜索关键字") @RequestParam String keyword,
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {

        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]全文搜索项目[{}]: keyword={}, page={}, size={}", 
                userId, projectId, keyword, page, size);

        // 参数验证
        if (keyword == null || keyword.trim().isEmpty()) {
            return R.fail("搜索关键字不能为空");
        }
        if (keyword.length() > 200) {
            return R.fail("搜索关键字过长，最多200个字符");
        }
        if (page < 0) {
            return R.fail("页码必须大于等于0");
        }
        if (size <= 0 || size > 100) {
            return R.fail("每页数量必须在1-100之间");
        }

        // 权限检查：必须是项目成员
        wikiSecurityUtils.requireProjectMember(projectId);

        Pageable pageable = PageRequest.of(page, size);
        Page<WikiSearchResultDTO> results = searchService.fullTextSearch(projectId, keyword.trim(), pageable);

        return R.ok(results);
    }

    /**
     * 简单全文搜索（不分页）
     * 权限要求：已登录 + 项目成员
     */
    @GetMapping("/projects/{projectId}/simple")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "简单全文搜索", description = "快速搜索，返回固定数量的结果")
    public R<List<WikiSearchResultDTO>> simpleSearch(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @Parameter(description = "搜索关键字") @RequestParam String keyword,
            @Parameter(description = "结果数量限制") @RequestParam(defaultValue = "20") int limit) {

        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]简单搜索项目[{}]: keyword={}, limit={}", 
                userId, projectId, keyword, limit);

        // 参数验证
        if (keyword == null || keyword.trim().isEmpty()) {
            return R.fail("搜索关键字不能为空");
        }
        if (keyword.length() > 200) {
            return R.fail("搜索关键字过长，最多200个字符");
        }
        if (limit <= 0 || limit > 100) {
            return R.fail("结果数量必须在1-100之间");
        }

        // 权限检查：必须是项目成员
        wikiSecurityUtils.requireProjectMember(projectId);

        List<WikiSearchResultDTO> results = searchService.simpleSearch(projectId, keyword.trim(), limit);

        return R.ok(results);
    }

    /**
     * 高级搜索（支持短语搜索和排除词）
     * 权限要求：已登录 + 项目成员
     */
    @GetMapping("/projects/{projectId}/advanced")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "高级搜索", description = "支持精确短语匹配和排除词的高级搜索")
    public R<Page<WikiSearchResultDTO>> advancedSearch(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @Parameter(description = "必须包含的词") @RequestParam(required = false) String includeWords,
            @Parameter(description = "必须排除的词") @RequestParam(required = false) String excludeWords,
            @Parameter(description = "精确短语") @RequestParam(required = false) String phrase,
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {

        Long userId = SecurityUtils.getUserId();
        log.info("用户[{}]高级搜索项目[{}]: include={}, exclude={}, phrase={}", 
                userId, projectId, includeWords, excludeWords, phrase);

        // 参数验证
        boolean hasInclude = includeWords != null && !includeWords.trim().isEmpty();
        boolean hasPhrase = phrase != null && !phrase.trim().isEmpty();
        
        if (!hasInclude && !hasPhrase) {
            return R.fail("至少需要提供包含词或精确短语之一");
        }
        if (page < 0) {
            return R.fail("页码必须大于等于0");
        }
        if (size <= 0 || size > 100) {
            return R.fail("每页数量必须在1-100之间");
        }

        // 权限检查：必须是项目成员
        wikiSecurityUtils.requireProjectMember(projectId);

        Pageable pageable = PageRequest.of(page, size);
        Page<WikiSearchResultDTO> results = searchService.advancedSearch(
                projectId, includeWords, excludeWords, phrase, pageable);

        return R.ok(results);
    }
}

