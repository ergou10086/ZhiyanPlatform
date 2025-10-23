package hbnu.project.zhiyanwiki.service;

import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import hbnu.project.zhiyanwiki.model.dto.WikiVersionDTO;
import hbnu.project.zhiyanwiki.model.entity.ChangeStats;
import hbnu.project.zhiyanwiki.model.entity.WikiContent;
import hbnu.project.zhiyanwiki.model.entity.WikiContentHistory;
import hbnu.project.zhiyanwiki.repository.WikiContentHistoryRepository;
import hbnu.project.zhiyanwiki.repository.WikiContentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Wiki内容服务
 * 负责Wiki内容的增删改查和版本管理
 *
 * @author ErgouTree
 */
@Service
@Slf4j
public class WikiContentService {

    @Autowired
    private WikiContentRepository contentRepo;

    @Autowired
    private WikiContentHistoryRepository historyRepo;

    @Autowired
    private DiffService diffService;

    // 保留最近 10 个版本
    private static final int MAX_RECENT_VERSIONS = 10;

    /**
     * 保存Wiki内容的新版本
     * 处理逻辑：
     * 1. 首次创建时初始化内容信息
     * 2. 内容无变化时跳过版本创建
     * 3. 内容有变化时创建新版本并维护版本历史
     * 4. 超过最近版本上限时归档旧版本
     *
     * @param wikiPageId        所属Wiki页面ID
     * @param projectId         所属项目ID
     * @param newContent        新的内容文本
     * @param changeDescription 版本变更描述
     * @param editorId          编辑者ID
     * @return 保存后的Wiki内容对象
     */
    @Transactional
    public WikiContent saveNewVersion(Long wikiPageId, Long projectId,
                                      String newContent, String changeDescription,
                                      Long editorId) {

        // 尝试获取已有内容，不存在则创建新对象
        WikiContent wikiContent = contentRepo.findByWikiPageId(wikiPageId)
                .orElse(new WikiContent());

        // 首次创建内容（ID为空表示新内容）
        if (wikiContent.getId() == null) {
            wikiContent.setWikiPageId(wikiPageId);
            wikiContent.setProjectId(projectId);
            wikiContent.setContent(newContent);
            wikiContent.setCurrentVersion(1);
            wikiContent.setContentHash(diffService.calculateHash(newContent));
            wikiContent.setRecentVersions(new ArrayList<>());
            wikiContent.setCreatedAt(LocalDateTime.now());
            wikiContent.setUpdatedAt(LocalDateTime.now());
            wikiContent.setLastEditorId(editorId);
            return contentRepo.save(wikiContent);
        }

        // 计算新内容的哈希值，与当前版本比对判断是否有实质变化
        String newHash = diffService.calculateHash(newContent);
        if (newHash.equals(wikiContent.getContentHash())) {
            log.info("内容未变化，跳过版本保存");
            return wikiContent;
        }

        // 计算新旧内容的差异及变更统计
        String oldContent = wikiContent.getContent();
        String diff = diffService.calculateDiff(oldContent, newContent);
        ChangeStats stats = diffService.calculateStats(oldContent, newContent);

        // 创建新版本记录
        int newVersion = wikiContent.getCurrentVersion() + 1;
        WikiContent.RecentVersion recentVersion = new WikiContent.RecentVersion();
        recentVersion.setVersion(newVersion);
        recentVersion.setContentDiff(diff);
        recentVersion.setChangeDescription(changeDescription);
        recentVersion.setEditorId(editorId);
        recentVersion.setCreatedAt(LocalDateTime.now());
        recentVersion.setAddedLines(stats.getAddedLines());
        recentVersion.setDeletedLines(stats.getDeletedLines());
        recentVersion.setChangedChars(stats.getChangedChars());

        // 维护最近版本列表
        List<WikiContent.RecentVersion> versions = wikiContent.getRecentVersions();
        if (versions == null) {
            versions = new ArrayList<>();
            wikiContent.setRecentVersions(versions);
        }
        versions.add(recentVersion);

        // 超过最大保留数量时，将最旧的版本归档到历史表
        if (versions.size() > MAX_RECENT_VERSIONS) {
            WikiContent.RecentVersion oldest = versions.remove(0);
            archiveOldVersion(wikiPageId, projectId, oldest);
        }

        // 更新当前内容为新版本
        wikiContent.setContent(newContent);
        wikiContent.setCurrentVersion(newVersion);
        wikiContent.setContentHash(newHash);
        wikiContent.setUpdatedAt(LocalDateTime.now());
        wikiContent.setLastEditorId(editorId);

        return contentRepo.save(wikiContent);
    }


    /**
     * 将超过最近版本上限的旧版本归档到历史表
     * 归档操作失败时仅记录日志，不影响主流程
     *
     * @param wikiPageId   Wiki页面ID
     * @param projectId    项目ID
     * @param recentVersion 要归档的版本
     */
    private void archiveOldVersion(Long wikiPageId, Long projectId, WikiContent.RecentVersion recentVersion) {
        try {
            // 构建历史版本实体并保存
            WikiContentHistory history = WikiContentHistory.builder()
                    .wikiPageId(wikiPageId)
                    .projectId(projectId)
                    .version(recentVersion.getVersion())
                    .contentDiff(recentVersion.getContentDiff())
                    .changeDescription(recentVersion.getChangeDescription())
                    .editorId(recentVersion.getEditorId())
                    // 保留原版本创建时间
                    .createdAt(recentVersion.getCreatedAt())
                    .addedLines(recentVersion.getAddedLines())
                    .deletedLines(recentVersion.getDeletedLines())
                    .changedChars(recentVersion.getChangedChars())
                    .contentHash(recentVersion.getContentHash())
                    // 记录归档时间
                    .archivedAt(LocalDateTime.now())
                    .build();

            historyRepo.save(history);
            log.info("归档旧版本成功: wikiPageId={}, version={}", wikiPageId, recentVersion.getVersion());
        } catch (ServiceException e) {
            log.error("归档旧版本失败: wikiPageId={}, version={}", wikiPageId, recentVersion.getVersion(), e);
            // 归档失败不中断主流程，仅记录错误日志
        }
    }


    /**
     * 获取指定版本的完整内容
     * 通过逆向应用差异补丁来重建历史版本
     *
     * @param wikiPageId    Wiki页面ID
     * @param targetVersion 目标版本号
     * @return 指定版本的完整内容
     */
    public String getVersionContent(Long wikiPageId, Integer targetVersion) {
        // 获取当前最新版本内容
        WikiContent current = contentRepo.findByWikiPageId(wikiPageId)
                .orElseThrow(() -> new ServiceException("Wiki content not found"));

        // 如果是当前版本，直接返回
        if (targetVersion.equals(current.getCurrentVersion())) {
            return current.getContent();
        }

        // 如果目标版本大于当前版本，抛出异常
        if (targetVersion > current.getCurrentVersion()) {
            throw new ServiceException("Target version does not exist");
        }

        // 从当前版本开始逆向应用差异补丁,进行逆向重建
        String content = current.getContent();
        int currentVer = current.getCurrentVersion();

        // 先处理最近版本列表中的版本（从新到旧逆向应用差异）
        List<WikiContent.RecentVersion> recentVersions = current.getRecentVersions();
        if (recentVersions != null) {
            for (int i = recentVersions.size() - 1; i >= 0 && currentVer > targetVersion; i--) {
                WikiContent.RecentVersion v = recentVersions.get(i);
                if (v.getVersion() == currentVer) {
                    // 逆向应用差异补丁
                    content = diffService.reversePatch(content, v.getContentDiff());
                    currentVer--;
                }
            }
        }

        // 如果目标版本更早，需要从历史集合中查询
        if (currentVer > targetVersion) {
            // 查询目标版本与当前重建版本之间的历史记录
            List<WikiContentHistory> histories = historyRepo.findByWikiPageIdAndVersionBetween(
                    wikiPageId, targetVersion + 1, currentVer
            );

            // 按版本号降序排序并逆向应用
            histories.sort((a, b) -> b.getVersion().compareTo(a.getVersion()));
            for (WikiContentHistory history : histories) {
                if (history.getVersion() == currentVer) {
                    content = diffService.reversePatch(content, history.getContentDiff());
                    currentVer--;
                }
            }
        }

        return content;
    }


    /**
     * 获取Wiki内容
     *
     * @param wikiPageId Wiki页面ID
     * @return Wiki内容
     */
    public WikiContent getContent(Long wikiPageId) {
        return contentRepo.findByWikiPageId(wikiPageId)
                .orElseThrow(() -> new ServiceException("Wiki content not found"));
    }


    /**
     * 获取最近的版本历史列表（最多10个版本）
     * 这些版本存储在主内容表中，未被归档
     *
     * @param wikiPageId Wiki页面ID
     * @return 最近版本历史列表
     */
    public List<WikiContent.RecentVersion> getRecentVersions(Long wikiPageId) {
        WikiContent content = contentRepo.findByWikiPageId(wikiPageId)
                .orElseThrow(() -> new ServiceException("Wiki content not found"));
        return content.getRecentVersions() != null ? content.getRecentVersions() : new ArrayList<>();
    }


    /**
     * 获取所有版本历史（包括最近版本和已归档版本）
     * 注意：返回的是混合类型列表（RecentVersion和WikiContentHistory）
     *
     * @param wikiPageId Wiki页面ID
     * @return 所有版本历史的混合列表
     */
    public List<Object> getAllVersionHistory(Long wikiPageId) {
        List<Object> allVersions = new ArrayList<>();

        // 获取最近10个版本（未归档的）
        WikiContent content = contentRepo.findByWikiPageId(wikiPageId).orElse(null);
        if (content != null && content.getRecentVersions() != null) {
            allVersions.addAll(content.getRecentVersions());
        }

        // 获取归档的历史版本
        List<WikiContentHistory> histories = historyRepo.findByWikiPageIdOrderByVersionDesc(wikiPageId);
        allVersions.addAll(histories);

        return allVersions;
    }


    /**
     * 比较两个版本之间的内容差异
     *
     * @param wikiPageId Wiki页面ID
     * @param version1   第一个版本号
     * @param version2   第二个版本号
     * @return 两个版本的差异文本
     */
    public String compareVersions(Long wikiPageId, Integer version1, Integer version2) {
        // 获取两个版本的完整内容
        String content1 = getVersionContent(wikiPageId, version1);
        String content2 = getVersionContent(wikiPageId, version2);
        // 计算并返回差异
        return diffService.calculateDiff(content1, content2);
    }


    /**
     * 删除指定Wiki页面的所有内容及版本历史
     * 包括主内容表和历史表的相关记录
     *
     * @param wikiPageId Wiki页面ID
     */
    @Transactional
    public void deleteContent(Long wikiPageId) {
        contentRepo.deleteByWikiPageId(wikiPageId);
        historyRepo.deleteByWikiPageId(wikiPageId);
        log.info("删除Wiki内容成功: wikiPageId={}", wikiPageId);
    }


    /**
     * 批量删除指定项目下的所有Wiki内容及历史
     * 用于项目或者目录删除时的级联操作
     *
     * @param projectId 项目ID
     */
    @Transactional
    public void deleteByProjectId(Long projectId) {
        contentRepo.deleteByProjectId(projectId);
        historyRepo.deleteByProjectId(projectId);
        log.info("删除项目Wiki内容成功: projectId={}", projectId);
    }


    /**
     * 全文搜索Wiki内容
     *
     * @param projectId 项目ID
     * @param keyword   关键字
     * @return 匹配的内容列表
     */
    public List<WikiContent> searchContent(Long projectId, String keyword) {
        return contentRepo.searchByContent(projectId, keyword);
    }


    /**
     * 获取版本历史列表（包含详细信息）
     *
     * @param wikiPageId Wiki页面ID
     * @return 版本历史DTO列表
     */
    public List<WikiVersionDTO> getVersionHistory(Long wikiPageId) {
        List<WikiVersionDTO> versions = new ArrayList<>();

        // 获取最近版本
        WikiContent content = contentRepo.findByWikiPageId(wikiPageId).orElse(null);
        if (content != null && content.getRecentVersions() != null) {
            for (WikiContent.RecentVersion rv : content.getRecentVersions()) {
                versions.add(WikiVersionDTO.builder()
                        .version(rv.getVersion())
                        .changeDescription(rv.getChangeDescription())
                        .editorId(rv.getEditorId() != null ? String.valueOf(rv.getEditorId()) : null)
                        .createdAt(rv.getCreatedAt())
                        .addedLines(rv.getAddedLines())
                        .deletedLines(rv.getDeletedLines())
                        .changedChars(rv.getChangedChars())
                        .isArchived(false)
                        .build());
            }
        }

        // 获取归档版本
        List<WikiContentHistory> histories = historyRepo.findByWikiPageIdOrderByVersionDesc(wikiPageId);
        for (WikiContentHistory history : histories) {
            versions.add(WikiVersionDTO.builder()
                    .version(history.getVersion())
                    .changeDescription(history.getChangeDescription())
                    .editorId(history.getEditorId() != null ? String.valueOf(history.getEditorId()) : null)
                    .createdAt(history.getCreatedAt())
                    .addedLines(history.getAddedLines())
                    .deletedLines(history.getDeletedLines())
                    .changedChars(history.getChangedChars())
                    .isArchived(true)
                    .build());
        }

        // 按版本号降序排序
        versions.sort((a, b) -> b.getVersion().compareTo(a.getVersion()));

        return versions;
    }


    /**
     * 获取版本差异对比—快捷方法
     *
     * @param wikiPageId Wiki页面ID
     * @param version1   版本1
     * @param version2   版本2
     * @return 差异文本
     */
    public String getVersionDiff(Long wikiPageId, Integer version1, Integer version2) {
        String content1 = getVersionContent(wikiPageId, version1);
        String content2 = getVersionContent(wikiPageId, version2);
        return diffService.calculateDiff(content1, content2);
    }
}
