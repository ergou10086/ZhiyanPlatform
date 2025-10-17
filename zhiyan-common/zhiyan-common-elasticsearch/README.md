# Zhiyan Common Elasticsearch 模块

## 概述

本模块为智研平台提供 Elasticsearch 9.1.4 全文检索功能支持。

## 版本说明

- **Elasticsearch**: 9.1.4
- **Elasticsearch Java Client**: 9.1.4
- **Spring Boot**: 3.2.4
- **Easy-ES**: 2.0.0-beta7

## 主要依赖

### 核心依赖

1. **Elasticsearch Java Client 9.1.4**
   - 官方推荐的新版 Java 客户端
   - 支持类型安全的 API
   - 完全支持 Elasticsearch 9.x 的所有特性

2. **Spring Data Elasticsearch**
   - Spring 生态的 Elasticsearch 集成
   - 提供便捷的 Repository 操作

3. **Easy-ES**
   - 国产 Elasticsearch ORM 框架
   - 简化复杂查询操作
   - 类似 MyBatis-Plus 的使用体验

4. **Jakarta JSON API & Yasson**
   - Elasticsearch 9.x 必需的 JSON 处理依赖
   - 提供 JSON-B 规范实现

## 配置说明

### 1. 基础配置（application.yml）

```yaml
spring:
  elasticsearch:
    # Elasticsearch 服务地址
    uris: 
      - http://localhost:9200
    # 认证信息（可选）
    username: elastic
    password: your-password
    # 超时配置
    connection-timeout: 5000
    socket-timeout: 60000
```

### 2. Easy-ES 配置

```yaml
easy-es:
  enable: true
  address: localhost:9200
  schema: http
  print-dsl: true
  global-config:
    index-prefix: zhiyan_
```

## 使用方式

### 方式一：使用 Elasticsearch Java Client（推荐）

```java
@Autowired
private ElasticsearchClient elasticsearchClient;

public void search() throws IOException {
    // 创建搜索请求
    SearchResponse<Document> response = elasticsearchClient.search(s -> s
        .index("my_index")
        .query(q -> q
            .match(m -> m
                .field("title")
                .query("搜索关键词")
            )
        ),
        Document.class
    );
    
    // 处理结果
    response.hits().hits().forEach(hit -> {
        Document doc = hit.source();
        // 处理文档
    });
}
```

### 方式二：使用 Spring Data Elasticsearch

```java
@Document(indexName = "my_index")
public class MyDocument {
    @Id
    private String id;
    
    @Field(type = FieldType.Text)
    private String title;
    
    @Field(type = FieldType.Keyword)
    private String category;
}

public interface MyDocumentRepository extends ElasticsearchRepository<MyDocument, String> {
    List<MyDocument> findByTitle(String title);
}
```

### 方式三：使用 Easy-ES

```java
@Data
@TableName("my_index")
public class Document {
    @TableId(type = IdType.CUSTOMIZE)
    private String id;
    
    @TableField(value = "title", fieldType = FieldType.TEXT)
    private String title;
}

public interface DocumentMapper extends BaseEsMapper<Document> {
}

// 使用
@Autowired
private DocumentMapper documentMapper;

public void query() {
    LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
    wrapper.eq(Document::getTitle, "测试");
    List<Document> list = documentMapper.selectList(wrapper);
}
```

## Elasticsearch 9.x 重要变更

### 1. 客户端变更
- **已废弃**: `RestHighLevelClient`（7.x 版本）
- **推荐使用**: `ElasticsearchClient`（新版 Java API Client）

### 2. 依赖变更
- 必须添加 `jakarta.json-api` 依赖
- 推荐使用 `yasson` 作为 JSON-B 实现

### 3. API 变更
- 使用 Lambda 风格的 Fluent API
- 更好的类型安全支持
- 更简洁的查询构建方式

## 集成到其他模块

在需要使用 Elasticsearch 的模块的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>hbnu.project</groupId>
    <artifactId>zhiyan-common-elasticsearch</artifactId>
</dependency>
```

## 注意事项

1. **版本兼容性**: 确保 Elasticsearch 服务器版本与客户端版本一致（都是 9.1.4）
2. **认证配置**: 生产环境务必启用安全认证
3. **连接池**: 已配置合理的连接池参数，可根据实际情况调整
4. **索引前缀**: 建议使用统一的索引前缀（如 `zhiyan_`）便于管理
5. **DSL 打印**: 开发环境可以开启 `print-dsl` 便于调试

## 常见问题

### Q1: 启动时报 `NoClassDefFoundError: jakarta/json/spi/JsonProvider`
**A**: 确保已添加 `jakarta.json-api` 和 `yasson` 依赖

### Q2: 连接超时
**A**: 检查 Elasticsearch 服务是否启动，调整 `connection-timeout` 和 `socket-timeout`

### Q3: 认证失败
**A**: 确认用户名密码正确，Elasticsearch 服务已启用安全认证

## 相关文档

- [Elasticsearch Java API Client 官方文档](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/index.html)
- [Spring Data Elasticsearch 官方文档](https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/)
- [Easy-ES 官方文档](https://www.easy-es.cn/)

