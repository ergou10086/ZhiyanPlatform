// 创建 wiki_contents 集合
db.createCollection("wiki_contents");

// 创建索引
db.wiki_contents.createIndex({ "wikiPageId": 1 }, { unique: true, name: "wikiPageId_unique" });
db.wiki_contents.createIndex({ "projectId": 1, "updatedAt": -1 }, { name: "idx_project_updated" });
db.wiki_contents.createIndex({ "wikiPageId": 1, "currentVersion": -1 }, { name: "idx_wiki_version" });
db.wiki_contents.createIndex({ "updatedAt": 1 }, { name: "updatedAt_index" });
db.wiki_contents.createIndex({ "content": "text" }, { name: "content_text_index" });

// 验证集合结构
db.wiki_contents.getIndexes();