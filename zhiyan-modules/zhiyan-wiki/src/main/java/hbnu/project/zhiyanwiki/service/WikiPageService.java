package hbnu.project.zhiyanwiki.service;

import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import hbnu.project.zhiyanwiki.model.dto.*;
import hbnu.project.zhiyanwiki.model.entity.WikiContent;
import hbnu.project.zhiyanwiki.model.entity.WikiPage;
import hbnu.project.zhiyanwiki.model.enums.PageType;
import hbnu.project.zhiyanwiki.repository.WikiPageRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Wiki内容服务类
 * 负责Wiki文档内容的管理，包括内容的增删改查、版本控制、历史记录管理等核心功能
 *
 * @author ErgouTree
 */
@Service
@Slf4j
public class WikiPageService {

    @Resource
    private WikiPageRepository wikiPageRepository;

    @Resource
    private WikiContentService contentService;

    @Resource
    private WikiMessageService wikiMessageService;

    /**
     * 创建 Wiki 页面（事务一致性策略）
     * 支持创建目录节点和文档节点
     */
    @Transactional
    public WikiPage createWikiPage(CreateWikiPageDTO dto) {
        // 1. 验证父页面（如果有）
        if (dto.getParentId() != null) {
            WikiPage parent = wikiPageRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("父页面不存在"));
            
            // 验证父页面是否是目录类型
            if (parent.getPageType() == PageType.DOCUMENT) {
                throw new RuntimeException("不能在文档节点下创建子节点，请选择目录节点");
            }
            
            // 验证是否属于同一项目
            if (!parent.getProjectId().equals(dto.getProjectId())) {
                throw new RuntimeException("父页面不属于该项目");
            }
        }

        // 2. 计算路径
        String path = calculatePath(dto.getProjectId(), dto.getParentId(), dto.getTitle());

        // 3. 计算排序序号
        Integer sortOrder = dto.getSortOrder();
        if (sortOrder == null) {
            sortOrder = wikiPageRepository.findMaxSortOrder(dto.getProjectId(), dto.getParentId()) + 1;
        }

        // 4. 在 MySQL 创建元数据
        WikiPage page = WikiPage.builder()
                .projectId(dto.getProjectId())
                .title(dto.getTitle())
                .pageType(dto.getPageType())
                .parentId(dto.getParentId())
                .path(path)
                .sortOrder(sortOrder)
                .isPublic(dto.getIsPublic())
                .creatorId(dto.getCreatorId())
                .lastEditorId(dto.getCreatorId())
                .build();
        page = wikiPageRepository.save(page);

        // 5. 如果是文档类型，在 MongoDB 创建内容
        if (dto.getPageType() == PageType.DOCUMENT) {
            try {
                WikiContent content = contentService.saveNewVersion(
                        page.getId(),
                        dto.getProjectId(),
                        dto.getContent() != null ? dto.getContent() : "",
                        StringUtils.hasText(dto.getChangeDescription()) ? 
                                dto.getChangeDescription() : "Initial creation",
                        dto.getCreatorId()
                );

                // 6. 回写 MongoDB ID 到 MySQL
                page.setMongoContentId(content.getId());
                page.setCurrentVersion(1);
                page.setContentSize(dto.getContent() != null ? dto.getContent().length() : 0);
                
                // 生成摘要（前200字符）
                if (dto.getContent() != null && !dto.getContent().isEmpty()) {
                    String summary = dto.getContent().length() > 200 ? 
                            dto.getContent().substring(0, 200) : dto.getContent();
                    page.setContentSummary(summary);
                }
                
                page = wikiPageRepository.save(page);

            } catch (Exception e) {
                log.error("MongoDB 创建失败，回滚 MySQL 记录", e);
                wikiPageRepository.delete(page);
                throw new RuntimeException("创建 Wiki 页面失败", e);
            }
        }

        log.info("创建Wiki页面成功: id={}, title={}, type={}", page.getId(), page.getTitle(), page.getPageType());

        // 7. 发送消息
        wikiMessageService.notifyWikiPageCreate(page, dto.getProjectId(),  dto.getCreatorId());

        return page;
    }


    /**
     * 更新Wiki页面
     *
     * @param pageId        页面ID
     * @param title         新标题
     * @param content       新内容
     * @param changeDesc    修改说明
     * @param editorId      编辑者ID
     * @return 更新后的页面
     */
    @Transactional
    public WikiPage updateWikiPage(Long pageId, String title, String content, 
                                   String changeDesc, Long editorId) {
        WikiPage page = wikiPageRepository.findById(pageId)
                .orElseThrow(() -> new RuntimeException("Wiki页面不存在"));

        // 更新标题
        if (StringUtils.hasText(title) && !title.equals(page.getTitle())) {
            page.setTitle(title);
            // 重新计算路径
            page.setPath(calculatePath(page.getProjectId(), page.getParentId(), title));
        }

        // 如果是文档类型，更新内容
        if (page.getPageType() == PageType.DOCUMENT && content != null) {
            WikiContent wikiContent = contentService.saveNewVersion(
                    pageId,
                    page.getProjectId(),
                    content,
                    StringUtils.hasText(changeDesc) ? changeDesc : "Update content",
                    editorId
            );

            page.setCurrentVersion(wikiContent.getCurrentVersion());
            page.setContentSize(content.length());
            
            // 更新摘要
            String summary = content.length() > 200 ? content.substring(0, 200) : content;
            page.setContentSummary(summary);
        }

        page.setLastEditorId(editorId);
        WikiPage saved = wikiPageRepository.save(page);

        log.info("更新Wiki页面成功: id={}, title={}", page.getId(), page.getTitle());

        wikiMessageService.notifyWikiPageUpdate(saved, saved.getProjectId(), editorId, changeDesc);

        return page;
    }


    /**
     * 更新页面排序
     *
     * @param pageId    页面ID
     * @param sortOrder 新的排序序号
     */
    @Transactional
    public void updateSortOrder(Long pageId, Integer sortOrder) {
        WikiPage page = wikiPageRepository.findById(pageId)
                .orElseThrow(() -> new ServiceException("Wiki页面不存在"));

        page.setSortOrder(sortOrder);
        wikiPageRepository.save(page);

        log.info("更新Wiki页面排序: pageId={}, sortOrder={}", pageId, sortOrder);
    }


    /**
     * 删除Wiki页面
     *
     * @param pageId 页面ID
     */
    @Transactional
    public void deleteWikiPage(Long pageId, Long operatorId) {
        WikiPage page = wikiPageRepository.findById(pageId)
                .orElseThrow(() -> new RuntimeException("Wiki页面不存在"));

        // 检查是否有子页面
        long childrenCount = wikiPageRepository.countByParentId(pageId);
        if (childrenCount > 0) {
            throw new RuntimeException("该页面下还有子页面，请先删除子页面");
        }

        // 删除内容（如果是文档类型）
        if (page.getPageType() == PageType.DOCUMENT && page.getMongoContentId() != null) {
            contentService.deleteContent(pageId);
        }

        // 删除元数据
        wikiPageRepository.delete(page);
        log.info("删除Wiki页面成功: id={}, title={}", page.getId(), page.getTitle());

        wikiMessageService.notifyWikiPageDelete(page, page.getProjectId(), operatorId);
    }


    /**
     * 递归删除目录及其所有子页面
     *
     * @param pageId 页面ID
     */
    @Transactional
    public void deletePageRecursively(Long pageId) {
        WikiPage page = wikiPageRepository.findById(pageId)
                .orElseThrow(() -> new ServiceException("Wiki页面不存在"));

        // 如果是目录，递归删除所有子页面
        if (page.getPageType() == PageType.DIRECTORY) {
            List<WikiPage> children = wikiPageRepository.findChildPages(page.getProjectId(), pageId);
            for (WikiPage child : children) {
                deletePageRecursively(child.getId());
            }
        }

        // 删除当前页面
        if (page.getPageType() == PageType.DOCUMENT && page.getMongoContentId() != null) {
            contentService.deleteContent(pageId);
        }

        wikiPageRepository.delete(page);
        log.info("递归删除Wiki页面: id={}, title={}", page.getId(), page.getTitle());
    }


    /**
     * 获取项目的Wiki树状结构
     *
     * @param projectId 项目ID
     * @return 树状结构列表
     */
    public List<WikiPageTreeDTO> getProjectWikiTree(Long projectId) {
        // 获取所有根页面
        List<WikiPage> rootPages = wikiPageRepository.findRootPages(projectId);
        
        // 构建树状结构
        return rootPages.stream()
                .map(this::buildWikiTree)
                .collect(Collectors.toList());
    }

    /**
     * 递归构建Wiki树
     *
     * @param page Wiki页面
     * @return 树节点DTO
     */
    private WikiPageTreeDTO buildWikiTree(WikiPage page) {
        WikiPageTreeDTO dto = WikiPageTreeDTO.builder()
                .id(String.valueOf(page.getId()))
                .title(page.getTitle())
                .parentId(page.getParentId() != null ? String.valueOf(page.getParentId()) : null)
                .path(page.getPath())
                .sortOrder(page.getSortOrder())
                .isPublic(page.getIsPublic())
                .pageType(page.getPageType().name())
                .currentVersion(page.getCurrentVersion())
                .contentSummary(page.getContentSummary())
                .createdAt(page.getCreatedAt() != null ? page.getCreatedAt().toString() : null)
                .updatedAt(page.getUpdatedAt() != null ? page.getUpdatedAt().toString() : null)
                .build();

        // 获取子页面
        List<WikiPage> children = wikiPageRepository.findChildPages(page.getProjectId(), page.getId());
        dto.setHasChildren(!children.isEmpty());
        dto.setChildrenCount(children.size());

        // 递归构建子树
        if (!children.isEmpty()) {
            List<WikiPageTreeDTO> childDtos = children.stream()
                    .map(this::buildWikiTree)
                    .collect(Collectors.toList());
            dto.setChildren(childDtos);
        } else {
            dto.setChildren(new ArrayList<>());
        }

        return dto;
    }

    /**
     * 计算页面路径
     *
     * @param projectId 项目ID
     * @param parentId  父页面ID
     * @param title     页面标题
     * @return 页面路径
     */
    private String calculatePath(Long projectId, Long parentId, String title) {
        if (parentId == null) {
            return "/" + title;
        }

        WikiPage parent = wikiPageRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("父页面不存在"));
        
        return parent.getPath() + "/" + title;
    }


    /**
     * 获取Wiki页面详情（包含内容）- 返回DTO
     *
     * @param pageId 页面ID
     * @return Wiki页面详情DTO
     */
    public WikiPageDetailDTO getWikiPageWithContent(Long pageId) {
        WikiPage page = wikiPageRepository.findById(pageId)
                .orElseThrow(() -> new ServiceException("Wiki页面不存在"));

        WikiPageDetailDTO.WikiPageDetailDTOBuilder builder = WikiPageDetailDTO.builder()
                .id(String.valueOf(page.getId()))
                .projectId(String.valueOf(page.getProjectId()))
                .title(page.getTitle())
                .pageType(page.getPageType().name())
                .parentId(page.getParentId() != null ? String.valueOf(page.getParentId()) : null)
                .path(page.getPath())
                .contentSummary(page.getContentSummary())
                .currentVersion(page.getCurrentVersion())
                .contentSize(page.getContentSize())
                .isPublic(page.getIsPublic())
                .isLocked(page.getIsLocked())
                .lockedBy(page.getLockedBy() != null ? String.valueOf(page.getLockedBy()) : null)
                .creatorId(String.valueOf(page.getCreatorId()))
                .lastEditorId(page.getLastEditorId() != null ? String.valueOf(page.getLastEditorId()) : null)
                .createdAt(page.getCreatedAt())
                .updatedAt(page.getUpdatedAt())
                .mongoContentId(page.getMongoContentId());
        
        // 如果是文档类型，获取内容
        if (page.getPageType() == PageType.DOCUMENT && page.getMongoContentId() != null) {
            try {
                WikiContent content = contentService.getContent(pageId);
                builder.content(content.getContent());
            } catch (Exception e) {
                log.error("获取Wiki内容失败: pageId={}", pageId, e);
                // 内容获取失败不影响其他元数据返回
            }
        }
        
        return builder.build();
    }


    /**
     * 搜索Wiki页面（根据标题）
     *
     * @param projectId 项目ID
     * @param keyword   搜索关键字
     * @param pageable  分页参数
     * @return 搜索结果分页
     */
    public Page<WikiSearchDTO> searchByTitle(Long projectId, String keyword, Pageable pageable) {
        Page<WikiPage> pages = wikiPageRepository.findByProjectIdAndTitleContaining(projectId, keyword, pageable);

        return pages.map(page -> WikiSearchDTO.builder()
                .id(String.valueOf(page.getId()))
                .title(page.getTitle())
                .path(page.getPath())
                .pageType(page.getPageType().name())
                .contentSummary(page.getContentSummary())
                .updatedAt(page.getUpdatedAt())
                .lastEditorId(page.getLastEditorId() != null ? String.valueOf(page.getLastEditorId()) : null)
                .build());
    }


    /**
     * 搜索Wiki页面（根据内容 - MongoDB全文搜索）
     *
     * @param projectId 项目ID
     * @param keyword   搜索关键字
     * @return 搜索结果列表
     */
    public List<WikiSearchDTO> searchByContent(Long projectId, String keyword) {
        log.info("[Wiki内容搜索] 开始搜索: projectId={}, keyword={}", projectId, keyword);
        
        // 从MongoDB搜索内容
        List<WikiContent> contents = contentService.searchContent(projectId, keyword);
        log.info("[Wiki内容搜索] MongoDB返回结果数: {}", contents.size());

        // 获取对应的Wiki页面元数据
        List<WikiSearchDTO> results = new ArrayList<>();
        for (WikiContent content : contents) {
            Optional<WikiPage> pageOpt = wikiPageRepository.findById(content.getWikiPageId());
            if (pageOpt.isPresent()) {
                WikiPage page = pageOpt.get();

                // 提取匹配内容的上下文作为摘要
                String summary = extractMatchContext(content.getContent(), keyword, 200);

                results.add(WikiSearchDTO.builder()
                        .id(String.valueOf(page.getId()))
                        .title(page.getTitle())
                        .path(page.getPath())
                        .pageType(page.getPageType().name())
                        .contentSummary(summary)
                        .updatedAt(page.getUpdatedAt())
                        .lastEditorId(page.getLastEditorId() != null ? String.valueOf(page.getLastEditorId()) : null)
                        .build());
            }
        }

        log.info("[Wiki内容搜索] 搜索完成: 返回{}个结果", results.size());
        return results;
    }


    /**
     * 提取匹配关键字的上下文
     *
     * @param content     完整内容
     * @param keyword     关键字
     * @param maxLength   最大长度
     * @return 上下文摘要
     */
    private String extractMatchContext(String content, String keyword, int maxLength) {
        if (content == null || keyword == null) {
            return "";
        }

        int index = content.toLowerCase().indexOf(keyword.toLowerCase());
        if (index == -1) {
            return content.length() > maxLength ? content.substring(0, maxLength) + "..." : content;
        }

        // 提取关键字前后的上下文
        int start = Math.max(0, index - maxLength / 2);
        int end = Math.min(content.length(), index + keyword.length() + maxLength / 2);

        String context = content.substring(start, end);
        if (start > 0) {
            context = "..." + context;
        }
        if (end < content.length()) {
            context = context + "...";
        }

        return context;
    }


    /**
     * 获取项目的Wiki统计信息
     *
     * @param projectId 项目ID
     * @return 统计信息
     */
    public WikiStatisticsDTO getProjectStatistics(Long projectId) {
        log.info("获取项目[{}]的Wiki统计信息", projectId);
        
        if (projectId == null) {
            log.warn("projectId为空，返回空统计信息");
            return WikiStatisticsDTO.builder()
                    .projectId("0")
                    .totalPages(0L)
                    .documentCount(0L)
                    .directoryCount(0L)
                    .totalContentSize(0L)
                    .contributorCount(0)
                    .recentUpdates(0L)
                    .totalVersions(0L)
                    .contributorStats(new HashMap<>())
                    .build();
        }
        
        // 获取所有页面
        List<WikiPage> allPages = wikiPageRepository.findByProjectId(projectId);
        log.debug("项目[{}]共有{}个Wiki页面", projectId, allPages.size());

        // 统计文档和目录数量
        long documentCount = allPages.stream()
                .filter(p -> p.getPageType() == PageType.DOCUMENT)
                .count();
        long directoryCount = allPages.stream()
                .filter(p -> p.getPageType() == PageType.DIRECTORY)
                .count();
        
        log.debug("项目[{}]文档数: {}, 目录数: {}", projectId, documentCount, directoryCount);

        // 统计总内容大小
        long totalContentSize = allPages.stream()
                .filter(p -> p.getContentSize() != null)
                .mapToLong(WikiPage::getContentSize)
                .sum();

        // 统计贡献者
        Set<Long> contributors = new HashSet<>();
        allPages.stream()
                .map(WikiPage::getCreatorId)
                .filter(Objects::nonNull)
                .forEach(contributors::add);
        allPages.stream()
                .map(WikiPage::getLastEditorId)
                .filter(Objects::nonNull)
                .forEach(contributors::add);

        // 统计最近30天更新
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long recentUpdates = allPages.stream()
                .filter(p -> p.getUpdatedAt() != null && p.getUpdatedAt().isAfter(thirtyDaysAgo))
                .count();

        // 统计各贡献者编辑次数（简化版）
        Map<String, Integer> contributorStats = new HashMap<>();
        for (WikiPage page : allPages) {
            if (page.getCreatorId() != null) {
                String creatorId = String.valueOf(page.getCreatorId());
                contributorStats.put(creatorId, contributorStats.getOrDefault(creatorId, 0) + 1);
            }
        }
        
        // 统计总版本数（需要查询MongoDB，这里简化处理）
        long totalVersions = 0L;
        try {
            for (WikiPage page : allPages) {
                if (page.getCurrentVersion() != null) {
                    totalVersions += page.getCurrentVersion();
                }
            }
        } catch (Exception e) {
            log.warn("统计总版本数时出错: {}", e.getMessage());
        }

        WikiStatisticsDTO result = WikiStatisticsDTO.builder()
                .projectId(String.valueOf(projectId))
                .totalPages((long) allPages.size())
                .documentCount(documentCount)
                .directoryCount(directoryCount)
                .totalContentSize(totalContentSize)
                .contributorCount(contributors.size())
                .recentUpdates(recentUpdates)
                .totalVersions(totalVersions)
                .contributorStats(contributorStats)
                .build();
        
        log.info("项目[{}]的Wiki统计信息: 总页面数={}, 文档数={}, 目录数={}, 贡献者数={}", 
                projectId, result.getTotalPages(), result.getDocumentCount(), 
                result.getDirectoryCount(), result.getContributorCount());
        
        return result;
    }


    /**
     * 复制Wiki页面
     *
     * @param sourcePageId 源页面ID
     * @param targetParentId 目标父页面ID（null表示根目录）
     * @param newTitle 新标题（null表示使用"副本-原标题"）
     * @param userId 操作用户ID
     * @return 新创建的页面
     */
    @Transactional
    public WikiPage copyPage(Long sourcePageId, Long targetParentId, String newTitle, Long userId) {
        WikiPage sourcePage = wikiPageRepository.findById(sourcePageId)
                .orElseThrow(() -> new ServiceException("源页面不存在"));

        // 生成新标题
        if (!StringUtils.hasText(newTitle)) {
            newTitle = "副本-" + sourcePage.getTitle();
        }

        // 创建DTO
        CreateWikiPageDTO dto = CreateWikiPageDTO.builder()
                .projectId(sourcePage.getProjectId())
                .title(newTitle)
                .pageType(sourcePage.getPageType())
                .parentId(targetParentId)
                .isPublic(sourcePage.getIsPublic())
                .creatorId(userId)
                .changeDescription("复制自: " + sourcePage.getTitle())
                .build();

        // 如果是文档类型，复制内容
        if (sourcePage.getPageType() == PageType.DOCUMENT && sourcePage.getMongoContentId() != null) {
            WikiContent sourceContent = contentService.getContent(sourcePageId);
            dto.setContent(sourceContent.getContent());
        }

        WikiPage newPage = createWikiPage(dto);

        log.info("复制Wiki页面: sourceId={}, newId={}", sourcePageId, newPage.getId());
        return newPage;
    }


    /**
     * 移动Wiki页面（修改父页面）
     *
     * @param pageId      页面ID
     * @param newParentId 新父页面ID
     */
    @Transactional
    public void moveWikiPage(Long pageId, Long newParentId, Long operatorId) {
        WikiPage page = wikiPageRepository.findById(pageId)
                .orElseThrow(() -> new ServiceException("Wiki页面不存在"));

        String oldPath = page.getPath();

        // 验证新父页面
        if (newParentId != null) {
            WikiPage newParent = wikiPageRepository.findById(newParentId)
                    .orElseThrow(() -> new ServiceException("新父页面不存在"));

            if (newParent.getPageType() == PageType.DOCUMENT) {
                throw new ServiceException("不能移动到文档节点下");
            }

            if (!newParent.getProjectId().equals(page.getProjectId())) {
                throw new ServiceException("不能移动到其他项目");
            }
        }

        // 更新父页面和路径
        page.setParentId(newParentId);
        page.setPath(calculatePath(page.getProjectId(), newParentId, page.getTitle()));
        wikiPageRepository.save(page);

        log.info("移动Wiki页面成功: id={}, newParentId={}", pageId, newParentId);

        wikiMessageService.notifyWikiPageMove(page, page.getProjectId(), operatorId, oldPath, page.getPath());
    }


    /**
     * 获取最近更新的Wiki页面
     *
     * @param projectId 项目ID
     * @param limit     数量限制
     * @return 最近更新的页面列表
     */
    public List<WikiPageTreeDTO> getRecentlyUpdated(Long projectId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<WikiPage> pages = wikiPageRepository.findRecentlyUpdated(projectId, pageable);

        return pages.stream()
                .map(page -> WikiPageTreeDTO.builder()
                        .id(String.valueOf(page.getId()))
                        .title(page.getTitle())
                        .path(page.getPath())
                        .pageType(page.getPageType().name())
                        .contentSummary(page.getContentSummary())
                        .currentVersion(page.getCurrentVersion())
                        .updatedAt(page.getUpdatedAt() != null ? page.getUpdatedAt().toString() : null)
                        .build())
                .collect(Collectors.toList());
    }
}
