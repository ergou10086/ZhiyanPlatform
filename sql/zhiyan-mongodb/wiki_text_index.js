// MongoDB Wiki全文索引初始化脚本
// 用于创建wiki_contents集合的文本索引

// 切换到zhiyan数据库
use zhiyan;

// 1. 创建wiki_contents集合（如果不存在）
db.createCollection("wiki_contents");

// 2. 创建文本索引（全文搜索）
db.wiki_contents.createIndex(
    { "content": "text" },
    {
        name: "idx_content_text",
        default_language: "none",  // 不使用特定语言的词干提取
        weights: {
            content: 10  // 内容字段的权重
        }
    }
);

// 3. 创建复合索引（提升查询性能）
db.wiki_contents.createIndex(
    { "projectId": 1, "updatedAt": -1 },
    { name: "idx_project_updated" }
);

db.wiki_contents.createIndex(
    { "wikiPageId": 1, "currentVersion": -1 },
    { name: "idx_wiki_version" }
);

// 4. 创建唯一索引
db.wiki_contents.createIndex(
    { "wikiPageId": 1 },
    { name: "idx_wiki_page_unique", unique: true }
);

// 5. 创建projectId索引
db.wiki_contents.createIndex(
    { "projectId": 1 },
    { name: "idx_project" }
);

// 6. 创建updatedAt索引
db.wiki_contents.createIndex(
    { "updatedAt": -1 },
    { name: "idx_updated_at" }
);

// 查看所有索引
print("=== Wiki Contents索引列表 ===");
db.wiki_contents.getIndexes().forEach(function(index) {
    printjson(index);
});

// 7. 测试全文搜索
print("\n=== 测试全文搜索 ===");
print("查询示例: db.wiki_contents.find({ $text: { $search: \"关键字\" } })");

// 查看集合统计
print("\n=== 集合统计信息 ===");
printjson(db.wiki_contents.stats());

print("\n✅ MongoDB Wiki全文索引初始化完成！");

