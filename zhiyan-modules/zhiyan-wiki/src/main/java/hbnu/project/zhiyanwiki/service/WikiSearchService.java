package hbnu.project.zhiyanwiki.service;

import hbnu.project.zhiyanwiki.model.dto.WikiSearchResultDTO;
import hbnu.project.zhiyanwiki.model.entity.WikiPage;
import hbnu.project.zhiyanwiki.repository.WikiPageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Wiki全文搜索服务
 * 基于MongoDB的文本索引实现高性能全文检索
 *
 * @author Tokito
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WikiSearchService {

    private final MongoTemplate mongoTemplate;
    private final WikiPageRepository wikiPageRepository;

    /**
     * 全文搜索Wiki内容（支持评分和分页）
     *
     * @param projectId 项目ID
     * @param keyword   搜索关键字
     * @param pageable  分页参数
     * @return 搜索结果分页
     */
    public Page<WikiSearchResultDTO> fullTextSearch(Long projectId, String keyword, Pageable pageable) {
        log.info("执行全文搜索: projectId={}, keyword={}, page={}", projectId, keyword, pageable.getPageNumber());

        // 构建文本搜索条件
        TextCriteria textCriteria = TextCriteria.forDefaultLanguage()
                .matchingAny(keyword);

        // 构建查询
        Query query = TextQuery.queryText(textCriteria)
                .sortByScore()  // 按相关性评分排序
                .with(pageable);

        // 添加项目ID过滤
        query.addCriteria(Criteria.where("projectId").is(projectId));

        // 包含评分信息
        query.fields()
                .include("wikiPageId", "projectId", "content", "updatedAt", "lastEditorId")
                .include("score");  // 包含搜索评分

        // 执行搜索
        List<Map> results = mongoTemplate.find(query, Map.class, "wiki_contents");

        // 统计总数
        long total = mongoTemplate.count(
                Query.query(textCriteria).addCriteria(Criteria.where("projectId").is(projectId)),
                "wiki_contents"
        );

        // 转换为DTO
        List<WikiSearchResultDTO> dtoList = convertToSearchResults(results, keyword);

        log.info("搜索完成: 找到{}个结果", dtoList.size());

        return new PageImpl<>(dtoList, pageable, total);
    }

    /**
     * 简单全文搜索（不分页）
     *
     * @param projectId 项目ID
     * @param keyword   搜索关键字
     * @param limit     结果数量限制
     * @return 搜索结果列表
     */
    public List<WikiSearchResultDTO> simpleSearch(Long projectId, String keyword, int limit) {
        TextCriteria textCriteria = TextCriteria.forDefaultLanguage()
                .matchingAny(keyword);

        Query query = TextQuery.queryText(textCriteria)
                .sortByScore()
                .limit(limit);

        query.addCriteria(Criteria.where("projectId").is(projectId));
        query.fields()
                .include("wikiPageId", "projectId", "content", "updatedAt", "lastEditorId")
                .include("score");

        List<Map> results = mongoTemplate.find(query, Map.class, "wiki_contents");

        return convertToSearchResults(results, keyword);
    }

    /**
     * 高级搜索（支持短语搜索和排除词）
     *
     * @param projectId     项目ID
     * @param includeWords  必须包含的词
     * @param excludeWords  必须排除的词
     * @param phrase        精确短语
     * @param pageable      分页参数
     * @return 搜索结果分页
     */
    public Page<WikiSearchResultDTO> advancedSearch(
            Long projectId,
            String includeWords,
            String excludeWords,
            String phrase,
            Pageable pageable) {

        // 构建文本搜索条件
        TextCriteria textCriteria = TextCriteria.forDefaultLanguage();

        if (includeWords != null && !includeWords.isEmpty()) {
            textCriteria.matchingAny(includeWords);
        }

        if (phrase != null && !phrase.isEmpty()) {
            textCriteria.matchingPhrase(phrase);
        }

        if (excludeWords != null && !excludeWords.isEmpty()) {
            textCriteria.notMatching(excludeWords);
        }

        Query query = TextQuery.queryText(textCriteria)
                .sortByScore()
                .with(pageable);

        query.addCriteria(Criteria.where("projectId").is(projectId));
        query.fields()
                .include("wikiPageId", "projectId", "content", "updatedAt", "lastEditorId")
                .include("score");

        List<Map> results = mongoTemplate.find(query, Map.class, "wiki_contents");

        long total = mongoTemplate.count(
                Query.query(textCriteria).addCriteria(Criteria.where("projectId").is(projectId)),
                "wiki_contents"
        );

        List<WikiSearchResultDTO> dtoList = convertToSearchResults(results, includeWords);

        return new PageImpl<>(dtoList, pageable, total);
    }

    /**
     * 转换MongoDB结果为DTO
     */
    private List<WikiSearchResultDTO> convertToSearchResults(List<Map> results, String keyword) {
        // 提取所有wikiPageId
        List<Long> wikiPageIds = results.stream()
                .map(map -> ((Number) map.get("wikiPageId")).longValue())
                .collect(Collectors.toList());

        // 批量查询Wiki页面信息
        List<WikiPage> wikiPages = wikiPageRepository.findAllById(wikiPageIds);
        Map<Long, WikiPage> pageMap = wikiPages.stream()
                .collect(Collectors.toMap(WikiPage::getId, Function.identity()));

        // 转换为DTO
        List<WikiSearchResultDTO> dtoList = new ArrayList<>();

        for (Map<String, Object> result : results) {
            Long wikiPageId = ((Number) result.get("wikiPageId")).longValue();
            WikiPage page = pageMap.get(wikiPageId);

            if (page == null) {
                continue;
            }

            // 提取匹配片段
            String content = (String) result.get("content");
            String snippet = extractSnippet(content, keyword, 200);

            // 获取搜索评分
            Double score = result.containsKey("score") ?
                    ((Number) result.get("score")).doubleValue() : 0.0;

            WikiSearchResultDTO dto = WikiSearchResultDTO.builder()
                    .wikiPageId(String.valueOf(wikiPageId))
                    .title(page.getTitle())
                    .path(page.getPath())
                    .matchedSnippet(snippet)
                    .score(score)
                    .matchCount(countMatches(content, keyword))
                    .updatedAt(page.getUpdatedAt())
                    .lastEditorId(page.getLastEditorId() != null ?
                            String.valueOf(page.getLastEditorId()) : null)
                    .projectId(String.valueOf(page.getProjectId()))
                    .build();

            dtoList.add(dto);
        }

        return dtoList;
    }

    /**
     * 提取匹配内容的上下文片段
     */
    private String extractSnippet(String content, String keyword, int maxLength) {
        if (content == null || keyword == null || keyword.isEmpty()) {
            return content != null && content.length() > maxLength ?
                    content.substring(0, maxLength) + "..." : content;
        }

        // 查找关键字位置（不区分大小写）
        int index = content.toLowerCase().indexOf(keyword.toLowerCase());

        if (index == -1) {
            // 如果没找到，尝试查找关键字的第一个词
            String[] words = keyword.split("\\s+");
            for (String word : words) {
                index = content.toLowerCase().indexOf(word.toLowerCase());
                if (index != -1) {
                    break;
                }
            }
        }

        if (index == -1) {
            // 还是没找到，返回开头
            return content.length() > maxLength ?
                    content.substring(0, maxLength) + "..." : content;
        }

        // 提取关键字周围的上下文
        int start = Math.max(0, index - maxLength / 2);
        int end = Math.min(content.length(), index + keyword.length() + maxLength / 2);

        String snippet = content.substring(start, end);

        // 添加省略号
        if (start > 0) {
            snippet = "..." + snippet;
        }
        if (end < content.length()) {
            snippet = snippet + "...";
        }

        // 高亮关键字
        snippet = highlightKeyword(snippet, keyword);

        return snippet;
    }

    /**
     * 高亮关键字（优化版：避免重复高亮，保持HTML标签完整性）
     */
    private String highlightKeyword(String text, String keyword) {
        if (text == null || keyword == null || keyword.trim().isEmpty()) {
            return text;
        }

        // 使用<mark>标签包裹关键字（更符合HTML5语义）
        String[] words = keyword.trim().split("\\s+");
        
        // 过滤掉过短的词（避免误匹配）
        List<String> validWords = new ArrayList<>();
        for (String word : words) {
            if (word.length() >= 2) {  // 至少2个字符才高亮
                validWords.add(word);
            }
        }
        
        if (validWords.isEmpty()) {
            return text;
        }

        // 按词长度降序排序（先匹配长词，避免短词被长词包含时重复高亮）
        validWords.sort((a, b) -> b.length() - a.length());

        // 使用正则表达式进行不区分大小写的替换
        for (String word : validWords) {
            // 使用\b单词边界，避免匹配到词的一部分
            // (?!<mark>) 负向前瞻断言，避免重复高亮已标记的内容
            text = text.replaceAll(
                    "(?i)(?!<mark>)\\b(" + java.util.regex.Pattern.quote(word) + ")\\b(?!</mark>)",
                    "<mark>$1</mark>"
            );
        }

        return text;
    }

    /**
     * 统计关键字出现次数
     */
    private Integer countMatches(String content, String keyword) {
        if (content == null || keyword == null) {
            return 0;
        }

        int count = 0;
        String lowerContent = content.toLowerCase();
        String[] words = keyword.toLowerCase().split("\\s+");

        for (String word : words) {
            int index = 0;
            while ((index = lowerContent.indexOf(word, index)) != -1) {
                count++;
                index += word.length();
            }
        }

        return count;
    }
}

