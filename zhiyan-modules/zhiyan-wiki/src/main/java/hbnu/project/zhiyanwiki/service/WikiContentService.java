package hbnu.project.zhiyanwiki.service;

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
     * 保存新版本
     */
    @Transactional
    public WikiContent saveNewVersion(Long wikiPageId, Long projectId,
                                      String newContent, String changeDescription,
                                      Long editorId) {
        WikiContent wikiContent = contentRepo.findByWikiPageId(wikiPageId)
                .orElse(new WikiContent());

        // 首次创建
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

        // 检查内容是否真的变化
        String newHash = diffService.calculateHash(newContent);
        if (newHash.equals(wikiContent.getContentHash())) {
            log.info("内容未变化，跳过版本保存");
            return wikiContent;
        }

        // 计算差异
        String oldContent = wikiContent.getContent();
        String diff = diffService.calculateDiff(oldContent, newContent);
        DiffService.ChangeStats stats = diffService.calculateStats(oldContent, newContent);

        // 创建新版本
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

        // 管理版本数组
        List<WikiContent.RecentVersion> versions = wikiContent.getRecentVersions();
        if (versions == null) {
            versions = new ArrayList<>();
            wikiContent.setRecentVersions(versions);
        }
        versions.add(recentVersion);

        // 如果超过限制，将最旧的版本移到历史集合
        if (versions.size() > MAX_RECENT_VERSIONS) {
            WikiContent.RecentVersion oldest = versions.remove(0);
            archiveOldVersion(wikiPageId, projectId, oldest);
        }

        // 更新主文档
        wikiContent.setContent(newContent);
        wikiContent.setCurrentVersion(newVersion);
        wikiContent.setContentHash(newHash);
        wikiContent.setUpdatedAt(LocalDateTime.now());
        wikiContent.setLastEditorId(editorId);

        return contentRepo.save(wikiContent);
    }

    /**
     * 将旧版本归档到历史集合
     *
     * @param wikiPageId   Wiki页面ID
     * @param projectId    项目ID
     * @param recentVersion 要归档的版本
     */
    private void archiveOldVersion(Long wikiPageId, Long projectId, WikiContent.RecentVersion recentVersion) {
        try {
            WikiContentHistory history = WikiContentHistory.builder()
                    .wikiPageId(wikiPageId)
                    .projectId(projectId)
                    .version(recentVersion.getVersion())
                    .contentDiff(recentVersion.getContentDiff())
                    .changeDescription(recentVersion.getChangeDescription())
                    .editorId(recentVersion.getEditorId())
                    .createdAt(recentVersion.getCreatedAt())
                    .addedLines(recentVersion.getAddedLines())
                    .deletedLines(recentVersion.getDeletedLines())
                    .changedChars(recentVersion.getChangedChars())
                    .contentHash(recentVersion.getContentHash())
                    .archivedAt(LocalDateTime.now())
                    .build();

            historyRepo.save(history);
            log.info("归档旧版本成功: wikiPageId={}, version={}", wikiPageId, recentVersion.getVersion());
        } catch (Exception e) {
            log.error("归档旧版本失败: wikiPageId={}, version={}", wikiPageId, recentVersion.getVersion(), e);
            // 归档失败不影响主流程，只记录日志
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
        WikiContent current = contentRepo.findByWikiPageId(wikiPageId)
                .orElseThrow(() -> new RuntimeException("Wiki content not found"));

        // 如果是当前版本，直接返回
        if (targetVersion.equals(current.getCurrentVersion())) {
            return current.getContent();
        }

        // 如果目标版本大于当前版本，抛出异常
        if (targetVersion > current.getCurrentVersion()) {
            throw new RuntimeException("Target version does not exist");
        }

        // 从当前版本开始逆向应用差异补丁
        String content = current.getContent();
        int currentVer = current.getCurrentVersion();

        // 先检查最近版本数组（从新到旧逆向应用）
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
            // 查询历史记录并继续逆向重建
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
                .orElseThrow(() -> new RuntimeException("Wiki content not found"));
    }

    /**
     * 获取版本历史列表（包含最近10个版本）
     *
     * @param wikiPageId Wiki页面ID
     * @return 版本历史列表
     */
    public List<WikiContent.RecentVersion> getRecentVersions(Long wikiPageId) {
        WikiContent content = contentRepo.findByWikiPageId(wikiPageId)
                .orElseThrow(() -> new RuntimeException("Wiki content not found"));
        return content.getRecentVersions() != null ? content.getRecentVersions() : new ArrayList<>();
    }

    /**
     * 获取所有版本历史（包括归档的版本）
     *
     * @param wikiPageId Wiki页面ID
     * @return 所有版本历史
     */
    public List<Object> getAllVersionHistory(Long wikiPageId) {
        List<Object> allVersions = new ArrayList<>();

        // 获取最近10个版本
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
     * 比较两个版本的差异
     *
     * @param wikiPageId Wiki页面ID
     * @param version1   版本1
     * @param version2   版本2
     * @return 差异内容
     */
    public String compareVersions(Long wikiPageId, Integer version1, Integer version2) {
        String content1 = getVersionContent(wikiPageId, version1);
        String content2 = getVersionContent(wikiPageId, version2);
        return diffService.calculateDiff(content1, content2);
    }

    /**
     * 删除Wiki内容
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
     * 删除项目下的所有Wiki内容
     *
     * @param projectId 项目ID
     */
    @Transactional
    public void deleteByProjectId(Long projectId) {
        contentRepo.deleteByProjectId(projectId);
        historyRepo.deleteByProjectId(projectId);
        log.info("删除项目Wiki内容成功: projectId={}", projectId);
    }
}
