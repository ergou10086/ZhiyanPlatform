// 知识库部分的 MongoDB 设计

--
    // Wiki文档集合（wiki_documents）
{
    _id: ObjectId("..."),
    project_id: 123,  // 对应MySQL的项目ID
    title: "技术文档标题",
    slug: "tech-doc-url-slug",  // URL友好的标识符

    // 文档内容
    content: "# Markdown内容\n...",  // 当前版本的内容
    content_html: "<h1>渲染后的HTML</h1>...",  // 缓存的HTML（可选）

    // 树状结构
    parent_id: ObjectId("..."),  // 父文档ID（null表示根文档）
    order_index: 1,  // 同级排序

    // 元数据
    creator_id: 456,
    last_editor_id: 789,

    // 附件引用（存储在MinIO）
    attachments: [
    {
        file_name: "diagram.png",
        minio_url: "http://minio/wiki-assets/...",
        bucket_name: "wiki-assets",
        object_key: "project-123/diagram.png"
    }
],

    // 标签和分类
    tags: ["API", "后端", "Spring"],

    // 时间戳
    created_at: ISODate("2025-01-15T10:00:00Z"),
    updated_at: ISODate("2025-10-17T14:30:00Z"),

    // 索引同步标记
    es_synced: true,
    es_sync_at: ISODate("2025-10-17T14:31:00Z")
}

// 索引
db.wiki_documents.createIndex({ project_id: 1, slug: 1 }, { unique: true })
db.wiki_documents.createIndex({ project_id: 1, parent_id: 1, order_index: 1 })
db.wiki_documents.createIndex({ "tags": 1 })
db.wiki_documents.createIndex({ updated_at: -1 })