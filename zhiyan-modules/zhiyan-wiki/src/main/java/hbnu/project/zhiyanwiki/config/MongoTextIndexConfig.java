package hbnu.project.zhiyanwiki.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.stereotype.Component;

/**
 * MongoDB文本索引配置
 * 自动创建全文检索索引
 *
 * @author Tokito
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MongoTextIndexConfig {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void initIndexes() {
        try {
            // 检查并创建wiki_contents集合的文本索引
            String collectionName = "wiki_contents";
            
            // 创建文本索引：对content字段建立全文索引
            TextIndexDefinition textIndex = TextIndexDefinition.builder()
                    .onField("content")
                    .build();

            try {
                mongoTemplate.indexOps(collectionName).ensureIndex(textIndex);
                log.info("MongoDB文本索引创建成功: collection={}, field=content", collectionName);
            } catch (Exception e) {
                log.warn("文本索引可能已存在: {}", e.getMessage());
            }

            // 创建复合索引以提升查询性能
            mongoTemplate.indexOps(collectionName)
                    .ensureIndex(new org.springframework.data.mongodb.core.index.Index()
                            .on("projectId", org.springframework.data.domain.Sort.Direction.ASC)
                            .on("updatedAt", org.springframework.data.domain.Sort.Direction.DESC));

            log.info("MongoDB索引初始化完成");

        } catch (Exception e) {
            log.error("MongoDB索引初始化失败", e);
        }
    }
}

