package hbnu.project.zhiyancommonelasticsearch.util;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Elasticsearch 9.1.4 工具类
 * 封装常用的 Elasticsearch 操作
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchUtil {

    private final ElasticsearchClient elasticsearchClient;

    /**
     * 判断索引是否存在
     */
    public boolean indexExists(String indexName) throws IOException {
        ExistsRequest request = ExistsRequest.of(e -> e.index(indexName));
        BooleanResponse response = elasticsearchClient.indices().exists(request);
        return response.value();
    }

    /**
     * 创建索引
     */
    public boolean createIndex(String indexName) throws IOException {
        if (indexExists(indexName)) {
            log.warn("索引已存在: {}", indexName);
            return false;
        }
        
        CreateIndexRequest request = CreateIndexRequest.of(c -> c.index(indexName));
        var response = elasticsearchClient.indices().create(request);
        log.info("创建索引成功: {}, acknowledged: {}", indexName, response.acknowledged());
        return response.acknowledged();
    }

    /**
     * 删除索引
     */
    public boolean deleteIndex(String indexName) throws IOException {
        if (!indexExists(indexName)) {
            log.warn("索引不存在: {}", indexName);
            return false;
        }
        
        DeleteIndexRequest request = DeleteIndexRequest.of(d -> d.index(indexName));
        var response = elasticsearchClient.indices().delete(request);
        log.info("删除索引成功: {}, acknowledged: {}", indexName, response.acknowledged());
        return response.acknowledged();
    }

    /**
     * 添加文档
     */
    public <T> String indexDocument(String indexName, String id, T document) throws IOException {
        IndexRequest<T> request = IndexRequest.of(i -> i
            .index(indexName)
            .id(id)
            .document(document)
        );
        
        IndexResponse response = elasticsearchClient.index(request);
        log.info("添加文档成功, 索引: {}, ID: {}, 结果: {}", indexName, id, response.result());
        return response.id();
    }

    /**
     * 根据 ID 获取文档
     */
    public <T> T getDocument(String indexName, String id, Class<T> clazz) throws IOException {
        GetRequest request = GetRequest.of(g -> g
            .index(indexName)
            .id(id)
        );
        
        GetResponse<T> response = elasticsearchClient.get(request, clazz);
        if (response.found()) {
            log.info("获取文档成功, 索引: {}, ID: {}", indexName, id);
            return response.source();
        } else {
            log.warn("文档不存在, 索引: {}, ID: {}", indexName, id);
            return null;
        }
    }

    /**
     * 更新文档
     */
    public <T> boolean updateDocument(String indexName, String id, T document) throws IOException {
        UpdateRequest<T, T> request = UpdateRequest.of(u -> u
            .index(indexName)
            .id(id)
            .doc(document)
        );
        
        UpdateResponse<T> response = elasticsearchClient.update(request, (Class<T>) document.getClass());
        log.info("更新文档成功, 索引: {}, ID: {}, 结果: {}", indexName, id, response.result());
        return response.result().jsonValue().equals("updated");
    }

    /**
     * 删除文档
     */
    public boolean deleteDocument(String indexName, String id) throws IOException {
        DeleteRequest request = DeleteRequest.of(d -> d
            .index(indexName)
            .id(id)
        );
        
        DeleteResponse response = elasticsearchClient.delete(request);
        log.info("删除文档, 索引: {}, ID: {}, 结果: {}", indexName, id, response.result());
        return response.result().jsonValue().equals("deleted");
    }

    /**
     * 搜索文档（全文搜索）
     */
    public <T> List<T> searchByMatchQuery(String indexName, String field, String queryText, Class<T> clazz) throws IOException {
        SearchRequest request = SearchRequest.of(s -> s
            .index(indexName)
            .query(q -> q
                .match(m -> m
                    .field(field)
                    .query(queryText)
                )
            )
        );
        
        SearchResponse<T> response = elasticsearchClient.search(request, clazz);
        log.info("搜索完成, 索引: {}, 命中数: {}", indexName, response.hits().total().value());
        
        return response.hits().hits().stream()
            .map(Hit::source)
            .collect(Collectors.toList());
    }

    /**
     * 搜索文档（精确匹配）
     */
    public <T> List<T> searchByTermQuery(String indexName, String field, String value, Class<T> clazz) throws IOException {
        SearchRequest request = SearchRequest.of(s -> s
            .index(indexName)
            .query(q -> q
                .term(t -> t
                    .field(field)
                    .value(value)
                )
            )
        );
        
        SearchResponse<T> response = elasticsearchClient.search(request, clazz);
        log.info("精确搜索完成, 索引: {}, 命中数: {}", indexName, response.hits().total().value());
        
        return response.hits().hits().stream()
            .map(Hit::source)
            .collect(Collectors.toList());
    }

    /**
     * 复合搜索（使用自定义查询）
     */
    public <T> List<T> searchByCustomQuery(String indexName, Query query, Class<T> clazz) throws IOException {
        SearchRequest request = SearchRequest.of(s -> s
            .index(indexName)
            .query(query)
        );
        
        SearchResponse<T> response = elasticsearchClient.search(request, clazz);
        log.info("自定义查询完成, 索引: {}, 命中数: {}", indexName, response.hits().total().value());
        
        return response.hits().hits().stream()
            .map(Hit::source)
            .collect(Collectors.toList());
    }

    /**
     * 批量索引文档
     */
    public <T> boolean bulkIndex(String indexName, List<T> documents, java.util.function.Function<T, String> idExtractor) throws IOException {
        BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();
        
        for (T doc : documents) {
            String id = idExtractor.apply(doc);
            bulkBuilder.operations(op -> op
                .index(idx -> idx
                    .index(indexName)
                    .id(id)
                    .document(doc)
                )
            );
        }
        
        BulkResponse response = elasticsearchClient.bulk(bulkBuilder.build());
        
        if (response.errors()) {
            log.error("批量索引存在错误");
            response.items().forEach(item -> {
                if (item.error() != null) {
                    log.error("错误项: {}, 错误信息: {}", item.id(), item.error().reason());
                }
            });
            return false;
        }
        
        log.info("批量索引成功, 索引: {}, 文档数: {}", indexName, documents.size());
        return true;
    }
}



































