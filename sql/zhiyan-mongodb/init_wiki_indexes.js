/**
 * Wiki模块 - MongoDB索引初始化脚本
 * 
 * 使用方法：
 * mongo mongodb://root:root@localhost:27017/zhiyan_wiki?authSource=admin init_wiki_indexes.js
 * 
 * 或者在mongo shell中执行：
 * use zhiyan_wiki
 * load('init_wiki_indexes.js')
 */

// 切换到wiki数据库
db = db.getSiblingDB('zhiyan_wiki');

print("======================================");
print("初始化Wiki模块MongoDB索引");
print("======================================");

// ==================== wiki_contents 集合索引 ====================
print("\n[1/2] 创建 wiki_contents 集合索引...");

// 1. 唯一索引：WikiPage ID（确保一个Wiki页面只有一个内容文档）
print("  - 创建唯一索引: wikiPageId");
db.wiki_contents.createIndex(
    { "wikiPageId": 1 }, 
    { 
        unique: true,
        name: "idx_unique_wiki_page_id",
        background: true
    }
);

// 2. 复合索引：项目ID + 更新时间（支持"最近更新"查询）
print("  - 创建复合索引: projectId + updatedAt");
db.wiki_contents.createIndex(
    { "projectId": 1, "updatedAt": -1 },
    {
        name: "idx_project_updated",
        background: true
    }
);

// 3. 复合索引：WikiPage ID + 版本号（支持版本查询）
print("  - 创建复合索引: wikiPageId + currentVersion");
db.wiki_contents.createIndex(
    { "wikiPageId": 1, "currentVersion": -1 },
    {
        name: "idx_wiki_version",
        background: true
    }
);

// 4. 文本索引：content字段（全文搜索，最重要！）
print("  - 创建文本索引: content (全文搜索)");
db.wiki_contents.createIndex(
    { "content": "text" },
    {
        name: "idx_fulltext_content",
        default_language: "none",  // 支持中英文混合搜索
        weights: { "content": 10 },  // 内容字段权重
        background: true
    }
);

// 5. 项目ID索引（支持项目级别查询）
print("  - 创建索引: projectId");
db.wiki_contents.createIndex(
    { "projectId": 1 },
    {
        name: "idx_project",
        background: true
    }
);

print("✓ wiki_contents 索引创建完成");

// ==================== wiki_content_history 集合索引 ====================
print("\n[2/2] 创建 wiki_content_history 集合索引...");

// 1. 复合索引：WikiPage ID + 版本号（查询历史版本）
print("  - 创建复合索引: wikiPageId + version");
db.wiki_content_history.createIndex(
    { "wikiPageId": 1, "version": -1 },
    {
        name: "idx_wiki_version_history",
        background: true
    }
);

// 2. 项目ID索引（项目级别操作）
print("  - 创建索引: projectId");
db.wiki_content_history.createIndex(
    { "projectId": 1 },
    {
        name: "idx_project_history",
        background: true
    }
);

// 3. 创建时间索引（时间范围查询）
print("  - 创建索引: createdAt");
db.wiki_content_history.createIndex(
    { "createdAt": -1 },
    {
        name: "idx_created_at",
        background: true
    }
);

// 4. 归档时间索引（清理过期数据）
print("  - 创建索引: archivedAt");
db.wiki_content_history.createIndex(
    { "archivedAt": -1 },
    {
        name: "idx_archived_at",
        background: true
    }
);

print("✓ wiki_content_history 索引创建完成");

// ==================== 验证索引 ====================
print("\n======================================");
print("索引创建汇总");
print("======================================");

print("\nwiki_contents 集合索引：");
var contentsIndexes = db.wiki_contents.getIndexes();
contentsIndexes.forEach(function(index) {
    print("  - " + index.name + ": " + JSON.stringify(index.key));
});

print("\nwiki_content_history 集合索引：");
var historyIndexes = db.wiki_content_history.getIndexes();
historyIndexes.forEach(function(index) {
    print("  - " + index.name + ": " + JSON.stringify(index.key));
});

// ==================== 性能测试 ====================
print("\n======================================");
print("索引性能测试");
print("======================================");

// 测试全文搜索性能
print("\n测试全文搜索...");
var searchStart = new Date();
var searchResult = db.wiki_contents.find({ $text: { $search: "测试" } }).limit(10).toArray();
var searchEnd = new Date();
print("  - 搜索耗时: " + (searchEnd - searchStart) + "ms");
print("  - 结果数量: " + searchResult.length);

// 测试复合索引查询性能
print("\n测试复合索引查询...");
var queryStart = new Date();
var queryResult = db.wiki_contents.find({ "projectId": 1 }).sort({ "updatedAt": -1 }).limit(10).toArray();
var queryEnd = new Date();
print("  - 查询耗时: " + (queryEnd - queryStart) + "ms");
print("  - 结果数量: " + queryResult.length);

print("\n======================================");
print("✓ Wiki模块MongoDB索引初始化完成！");
print("======================================");

// ==================== 索引维护建议 ====================
print("\n【索引维护建议】");
print("1. 定期检查索引使用情况：db.wiki_contents.aggregate([{$indexStats:{}}])");
print("2. 定期重建索引（如果性能下降）：db.wiki_contents.reIndex()");
print("3. 监控集合大小：db.wiki_contents.stats()");
print("4. 清理过期历史版本（建议保留最近1年）：");
print("   db.wiki_content_history.deleteMany({");
print("     archivedAt: { $lt: new Date(Date.now() - 365*24*60*60*1000) }");
print("   })");
print("");

