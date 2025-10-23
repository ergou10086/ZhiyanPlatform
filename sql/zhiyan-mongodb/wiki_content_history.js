// 创建 wiki_content_history 集合
db.createCollection("wiki_content_history");

// 创建索引
db.wiki_content_history.createIndex({ "wikiPageId": 1, "version": -1 }, { name: "idx_wiki_version" });
db.wiki_content_history.createIndex({ "projectId": 1, "createdAt": -1 }, { name: "idx_project_created" });
db.wiki_content_history.createIndex({ "wikiPageId": 1 }, { name: "wikiPageId_index" });
db.wiki_content_history.createIndex({ "createdAt": 1 }, { name: "createdAt_index" });

// 验证集合结构
db.wiki_content_history.getIndexes();