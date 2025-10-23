package hbnu.project.zhiyanwiki.service;

import hbnu.project.zhiyanwiki.model.dto.CreateWikiPageDTO;
import hbnu.project.zhiyanwiki.model.dto.WikiPageTreeDTO;
import hbnu.project.zhiyanwiki.model.entity.WikiContent;
import hbnu.project.zhiyanwiki.model.entity.WikiPage;
import hbnu.project.zhiyanwiki.model.enums.PageType;
import hbnu.project.zhiyanwiki.repository.WikiPageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Wiki页面服务
 * 负责Wiki页面的增删改查和树状结构管理
 *
 * @author ErgouTree
 */
@Service
@Slf4j
public class WikiPageService {

    @Autowired
    private WikiPageRepository wikiPageRepository;

    @Autowired
    private WikiContentService contentService;

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
                if (dto.getContent() != null && dto.getContent().length() > 0) {
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
        page = wikiPageRepository.save(page);

        log.info("更新Wiki页面成功: id={}, title={}", page.getId(), page.getTitle());
        return page;
    }

    /**
     * 删除Wiki页面
     *
     * @param pageId 页面ID
     */
    @Transactional
    public void deleteWikiPage(Long pageId) {
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
                .build();

        // 获取子页面
        List<WikiPage> children = wikiPageRepository.findChildPages(page.getProjectId(), page.getId());
        dto.setHasChildren(!children.isEmpty());
        dto.setChildrenCount(children.size());

        // 设置图标
        dto.setIcon(page.getPageType() == PageType.DIRECTORY ? "folder" : "document");

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
     * 移动Wiki页面（修改父页面）
     *
     * @param pageId      页面ID
     * @param newParentId 新父页面ID
     */
    @Transactional
    public void moveWikiPage(Long pageId, Long newParentId) {
        WikiPage page = wikiPageRepository.findById(pageId)
                .orElseThrow(() -> new RuntimeException("Wiki页面不存在"));

        // 验证新父页面
        if (newParentId != null) {
            WikiPage newParent = wikiPageRepository.findById(newParentId)
                    .orElseThrow(() -> new RuntimeException("新父页面不存在"));
            
            if (newParent.getPageType() == PageType.DOCUMENT) {
                throw new RuntimeException("不能移动到文档节点下");
            }
            
            if (!newParent.getProjectId().equals(page.getProjectId())) {
                throw new RuntimeException("不能移动到其他项目");
            }
        }

        // 更新父页面和路径
        page.setParentId(newParentId);
        page.setPath(calculatePath(page.getProjectId(), newParentId, page.getTitle()));
        wikiPageRepository.save(page);

        log.info("移动Wiki页面成功: id={}, newParentId={}", pageId, newParentId);
    }

    /**
     * 获取Wiki页面详情（包含内容）
     *
     * @param pageId 页面ID
     * @return Wiki页面
     */
    public WikiPage getWikiPageWithContent(Long pageId) {
        WikiPage page = wikiPageRepository.findById(pageId)
                .orElseThrow(() -> new RuntimeException("Wiki页面不存在"));
        
        // 如果是文档类型，获取内容
        if (page.getPageType() == PageType.DOCUMENT && page.getMongoContentId() != null) {
            WikiContent content = contentService.getContent(pageId);
            // 注意：这里可以在page对象中添加一个transient字段来存储content
            // 或者返回一个包含page和content的DTO
        }
        
        return page;
    }
}
