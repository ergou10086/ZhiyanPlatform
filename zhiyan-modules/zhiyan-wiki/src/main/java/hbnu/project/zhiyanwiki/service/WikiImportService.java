package hbnu.project.zhiyanwiki.service;

import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import hbnu.project.zhiyanwiki.model.dto.CreateWikiPageDTO;
import hbnu.project.zhiyanwiki.model.dto.WikiImportDTO;
import hbnu.project.zhiyanwiki.model.dto.WikiImportResultDTO;
import hbnu.project.zhiyanwiki.model.entity.WikiPage;
import hbnu.project.zhiyanwiki.model.enums.PageType;
import hbnu.project.zhiyanwiki.repository.WikiPageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wiki导入服务
 * 提供从不同格式导入Wiki页面的功能
 *
 * @author Tokito
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WikiImportService {

    private final WikiPageRepository wikiPageRepository;
    private final WikiPageService wikiPageService;

    /**
     * 从Markdown文件导入
     *
     * @param file      Markdown文件
     * @param importDTO 导入配置
     * @return 导入结果
     */
    @Transactional(rollbackFor = Exception.class)
    public WikiImportResultDTO importFromMarkdown(MultipartFile file, WikiImportDTO importDTO) {
        WikiImportResultDTO result = WikiImportResultDTO.builder()
                .success(false)
                .build();

        try {
            // 读取文件内容
            String content = readFileContent(file);
            
            // 解析Markdown
            MarkdownPage mdPage = parseMarkdown(content);
            
            // 检查是否存在同名页面
            if (!importDTO.getOverwrite()) {
                List<WikiPage> existingPages = wikiPageRepository.findByProjectIdAndTitleAndParentId(
                        importDTO.getProjectId(), mdPage.getTitle(), importDTO.getParentId());
                if (!existingPages.isEmpty()) {
                    result.getErrors().add("页面已存在: " + mdPage.getTitle());
                    result.setMessage("导入失败：页面已存在且未设置覆盖模式");
                    return result;
                }
            }

            // 创建Wiki页面
            CreateWikiPageDTO createDTO = CreateWikiPageDTO.builder()
                    .projectId(importDTO.getProjectId())
                    .parentId(importDTO.getParentId())
                    .title(mdPage.getTitle())
                    .content(mdPage.getContent())
                    .pageType(PageType.DOCUMENT)
                    .isPublic(importDTO.getIsPublic())
                    .creatorId(importDTO.getImportBy())
                    .changeDescription("从Markdown导入")
                    .build();

            WikiPage createdPage = wikiPageService.createWikiPage(createDTO);

            result.setSuccess(true);
            result.setImportedCount(1);
            result.getPageIds().add(String.valueOf(createdPage.getId()));
            result.setMessage("导入成功：" + mdPage.getTitle());

            log.info("Markdown导入成功: pageId={}, title={}", createdPage.getId(), mdPage.getTitle());

        } catch (Exception e) {
            log.error("Markdown导入失败", e);
            result.setFailedCount(1);
            result.getErrors().add(e.getMessage());
            result.setMessage("导入失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 批量导入Markdown文件
     *
     * @param files     文件列表
     * @param importDTO 导入配置
     * @return 导入结果
     */
    @Transactional(rollbackFor = Exception.class)
    public WikiImportResultDTO importMultipleMarkdown(MultipartFile[] files, WikiImportDTO importDTO) {
        WikiImportResultDTO result = WikiImportResultDTO.builder()
                .success(true)
                .build();

        for (MultipartFile file : files) {
            try {
                WikiImportResultDTO singleResult = importFromMarkdown(file, importDTO);
                
                if (singleResult.getSuccess()) {
                    result.setImportedCount(result.getImportedCount() + 1);
                    result.getPageIds().addAll(singleResult.getPageIds());
                } else {
                    result.setFailedCount(result.getFailedCount() + 1);
                    result.getErrors().addAll(singleResult.getErrors());
                }
            } catch (Exception e) {
                log.error("导入文件失败: {}", file.getOriginalFilename(), e);
                result.setFailedCount(result.getFailedCount() + 1);
                result.getErrors().add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }

        if (result.getFailedCount() > 0) {
            result.setSuccess(false);
        }

        result.setMessage(String.format("导入完成：成功%d个，失败%d个", 
                result.getImportedCount(), result.getFailedCount()));

        return result;
    }

    /**
     * 读取文件内容
     */
    private String readFileContent(MultipartFile file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    /**
     * 解析Markdown内容
     */
    private MarkdownPage parseMarkdown(String content) {
        MarkdownPage page = new MarkdownPage();

        // 提取标题（第一个 # 标题）
        Pattern titlePattern = Pattern.compile("^#\\s+(.+)$", Pattern.MULTILINE);
        Matcher titleMatcher = titlePattern.matcher(content);
        
        if (titleMatcher.find()) {
            page.setTitle(titleMatcher.group(1).trim());
            
            // 移除标题后的内容作为正文
            int titleEnd = titleMatcher.end();
            String bodyContent = content.substring(titleEnd).trim();
            
            // 移除元数据部分（如果存在）
            bodyContent = removeMetadata(bodyContent);
            page.setContent(bodyContent);
        } else {
            // 如果没有标题，使用文件名或"未命名"
            page.setTitle("未命名文档");
            page.setContent(content);
        }

        return page;
    }

    /**
     * 移除Markdown中的元数据部分（YAML front matter）
     */
    private String removeMetadata(String content) {
        // 移除 --- ... --- 之间的内容
        Pattern metadataPattern = Pattern.compile("^---\\s*\\n.*?\\n---\\s*\\n", Pattern.DOTALL);
        Matcher matcher = metadataPattern.matcher(content);
        
        if (matcher.find()) {
            return content.substring(matcher.end()).trim();
        }
        
        return content;
    }

    /**
     * Markdown页面内部类
     */
    private static class MarkdownPage {
        private String title;
        private String content;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}

