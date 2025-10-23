// 切换到目标数据库
db = db.getSiblingDB('zhiyan_wiki');

// 创建 wiki_contents 集合和索引
db.createCollection("wiki_contents");
db.wiki_contents.createIndex({ "wikiPageId": 1 }, { unique: true, name: "wikiPageId_unique" });
db.wiki_contents.createIndex({ "projectId": 1, "updatedAt": -1 }, { name: "idx_project_updated" });
db.wiki_contents.createIndex({ "wikiPageId": 1, "currentVersion": -1 }, { name: "idx_wiki_version" });
db.wiki_contents.createIndex({ "updatedAt": 1 }, { name: "updatedAt_index" });
db.wiki_contents.createIndex({ "content": "text" }, { name: "content_text_index" });

// 创建 wiki_content_history 集合和索引
db.createCollection("wiki_content_history");
db.wiki_content_history.createIndex({ "wikiPageId": 1, "version": -1 }, { name: "idx_wiki_version" });
db.wiki_content_history.createIndex({ "projectId": 1, "createdAt": -1 }, { name: "idx_project_created" });
db.wiki_content_history.createIndex({ "wikiPageId": 1 }, { name: "wikiPageId_index" });
db.wiki_content_history.createIndex({ "createdAt": 1 }, { name: "createdAt_index" });

print("MongoDB 集合和索引创建完成");


// mongosh --file D:\WorkSpace\JavaDemo\Programe\ZhiyanPlatform\ZhiyanPlatform\sql\zhiyan-mongodb\mongodb-init.js