// ==================== wiki_contents.js 集合索引 ====================
// Wiki内容主集合，存储当前版本和最近10个历史版本

// 唯一索引：通过wikiPageId快速定位文档内容（一个页面对应一个内容文档）
db.wiki_contents.createIndex(
    { "wikiPageId": 1 },
    { 
        unique: true,
        name: "idx_wiki_page_unique",
        background: true
    }
);

// 复合索引：按项目查询最近更新的文档（用于项目Wiki列表展示）
db.wiki_contents.createIndex(
    { "projectId": 1, "updatedAt": -1 },
    {
        name: "idx_project_updated",
        background: true
    }
);

// 复合索引：支持按项目和版本号查询（用于版本管理）
db.wiki_contents.createIndex(
    { "wikiPageId": 1, "currentVersion": -1 },
    {
        name: "idx_wiki_version",
        background: true
    }
);

// 全文索引：支持Markdown内容的全文搜索
db.wiki_contents.createIndex(
    { "content": "text" },
    {
        name: "idx_content_fulltext",
        default_language: "none", // 支持中文搜索
        weights: { "content": 10 }, // 内容权重
        background: true
    }
);

// 单字段索引：支持按更新时间排序
db.wiki_contents.createIndex(
    { "updatedAt": -1 },
    {
        name: "idx_updated_desc",
        background: true
    }
);

// 单字段索引：支持按项目ID查询（用于项目级别的数据导出和统计）
db.wiki_contents.createIndex(
    { "projectId": 1 },
    {
        name: "idx_project",
        background: true
    }
);

// 协同编辑索引：查询有在线编辑者的文档
db.wiki_contents.createIndex(
    { "activeEditors": 1 },
    {
        name: "idx_active_editors",
        sparse: true, // 稀疏索引，仅索引有activeEditors字段的文档
        background: true
    }
);

// ==================== wiki_content_history 集合索引 ====================
// Wiki历史版本归档集合，存储超过10个版本后被归档的旧版本

// 复合索引：按页面和版本号查询历史（用于版本回溯和对比）
db.wiki_content_history.createIndex(
    { "wikiPageId": 1, "version": -1 },
    {
        name: "idx_wiki_version_desc",
        background: true
    }
);

// 复合索引：按项目和创建时间查询（用于项目历史统计）
db.wiki_content_history.createIndex(
    { "projectId": 1, "createdAt": -1 },
    {
        name: "idx_project_created",
        background: true
    }
);

// 单字段索引：按创建时间查询（用于清理策略）
db.wiki_content_history.createIndex(
    { "createdAt": -1 },
    {
        name: "idx_created_desc",
        background: true
    }
);

// TTL索引：自动清理超过180天的历史记录（可根据需求调整）
db.wiki_content_history.createIndex(
    { "createdAt": 1 },
    {
        name: "idx_ttl_cleanup",
        expireAfterSeconds: 15552000, // 180天 = 180 * 24 * 60 * 60
        background: true
    }
);

// 单字段索引：按页面ID查询（用于快速定位某个页面的所有历史版本）
db.wiki_content_history.createIndex(
    { "wikiPageId": 1 },
    {
        name: "idx_wiki_page",
        background: true
    }
);

// 单字段索引：按编辑者查询（用于用户编辑历史统计）
db.wiki_content_history.createIndex(
    { "editorId": 1 },
    {
        name: "idx_editor",
        background: true
    }
);

// ==================== 索引统计和验证 ====================

// 查看wiki_contents集合的所有索引
print("==== wiki_contents.js 索引列表 ====");
printjson(db.wiki_contents.getIndexes());

// 查看wiki_content_history集合的所有索引
print("\n==== wiki_content_history 索引列表 ====");
printjson(db.wiki_content_history.getIndexes());

// 输出完成信息
print("\n✅ MongoDB索引创建完成！");
print("📊 wiki_contents.js 集合: " + db.wiki_contents.getIndexes().length + " 个索引");
print("📊 wiki_content_history 集合: " + db.wiki_content_history.getIndexes().length + " 个索引");
