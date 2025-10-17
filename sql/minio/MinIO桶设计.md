# MinIO 桶设计方案

## 1. 知识库部分的桶设计

### 1.1 achievement-files（成果附件桶）
用于存储各类科研成果的文件，支持版本控制。

```
achievement-files/
├── project-{project_id}/
│   ├── achievement-{achievement_id}/
│   │   ├── v1_{original_filename}.pdf
│   │   ├── v2_{original_filename}.pdf
│   │   ├── v3_{original_filename}.zip
│   │   └── ...
```

**存储规则：**
- 路径模板：`project-{project_id}/achievement-{achievement_id}/v{version}_{filename}`
- 对应表：`achievement_file`
- 文件类型：PDF、Word、Excel、ZIP、CSV、JSON 等
- 版本管理：通过 `version` 字段和 `is_latest` 标记
- 访问控制：项目成员可访问
- 生命周期：随 achievement 记录删除而删除

---

### 1.2 wiki-assets（Wiki 文档资源桶）
存储 Wiki 文档中引用的图片、附件等资源。

```
wiki-assets/
├── project-{project_id}/
│   ├── images/
│   │   ├── {document_id}/
│   │   │   ├── {timestamp}_{filename}.png
│   │   │   └── {timestamp}_{filename}.jpg
│   ├── attachments/
│   │   ├── {document_id}/
│   │   │   ├── {timestamp}_{filename}.pdf
│   │   │   └── {timestamp}_{filename}.docx
```

**存储规则：**
- 路径模板：`project-{project_id}/{type}/{document_id}/{timestamp}_{filename}`
- 对应集合：`wiki_documents.attachments`
- 文件类型：
    - images: PNG, JPG, GIF, SVG, WebP
    - attachments: PDF, DOCX, XLSX, ZIP, TXT
- 访问控制：与 Wiki 文档权限一致
- 清理策略：文档删除时异步清理相关资源

---

### 1.3 temp-uploads（临时上传桶）
用于文件上传的临时存储，定期清理未关联的文件。

```
temp-uploads/
├── user-{user_id}/
│   ├── {upload_session_id}/
│   │   ├── {random_uuid}_{filename}
│   │   └── ...
```

**存储规则：**
- 路径模板：`user-{user_id}/{upload_session_id}/{uuid}_{filename}`
- 生命周期：24小时后自动清理
- 使用场景：
    - 分片上传的临时存储
    - 预览前的文件暂存
    - 表单草稿的附件
- 清理策略：
    - 每日凌晨 2:00 执行清理任务
    - 删除创建时间超过 24 小时的文件
    - 保留最近 1 小时内的所有文件（防止正在上传）

---

## 2. 项目和知识库主图的桶设计(知识库主图和项目一样)

### 2.1 project-covers（项目封面图桶）
存储项目的封面图片和缩略图。

```
project-covers/
├── original/
│   └── project-{project_id}.{ext}
└── default/
    ├── default_science.jpg
    ├── default_engineering.jpg
    └── default_research.jpg
```

**存储规则：**
- 原图路径：`original/project-{project_id}.{ext}`
- 对应表：`projects` 表中的 `cover_image_url` 字段（JSON格式存储多个尺寸）
- 文件类型：JPG, PNG, WebP
- 访问控制：根据项目 visibility 决定
- 默认图：提供多套默认封面供选择

---

## 3. 用户头像的桶设计

### 3.1 user-avatars（用户头像桶）
存储用户头像及其缩略图。

```
user-avatars/
├── original/
│   └── user-{user_id}.{ext}             # 原始上传图片
└── default/
    ├── default_avatar_1.png
    ├── default_avatar_2.png
    └── ...default_avatar_10.png         # 10 套默认头像
```

**存储规则：**
- 原图路径：`original/user-{user_id}.{ext}`
- 缩略图路径：`thumbnail/user-{user_id}_{size}.jpg`
- 对应表：`users.avatar_url` 字段（存储 JSON，包含所有尺寸）
- 文件类型：JPG, PNG, WebP
- 图片规格：
    - 32x32: 评论、任务列表
    - 64x64: 成员列表、卡片
    - 128x128: 对话框、弹窗
    - 256x256: 个人主页、设置页
- 上传限制：
    - 最大 5MB
    - 推荐尺寸 512x512
    - 自动裁剪为正方形
- 访问控制：公开访问（CDN 加速）
- 默认头像：系统提供 10 套默认头像供选择

---

## 4. 桶配置和策略

### 4.1 访问策略配置

```json
{
  "achievement-files": {
    "access": "private",
    "policy": "project-member-only",
    "encryption": "AES256"
  },
  "wiki-assets": {
    "access": "conditional",
    "policy": "based-on-project-visibility",
    "cache": "7-days"
  },
  "temp-uploads": {
    "access": "private",
    "policy": "owner-only",
    "lifecycle": "24-hours"
  },
  "project-covers": {
    "access": "conditional",
    "policy": "based-on-project-visibility",
    "cache": "30-days",
    "cdn": true
  },
  "knowledge-covers": {
    "access": "conditional",
    "policy": "inherit-from-parent",
    "cache": "30-days"
  },
  "user-avatars": {
    "access": "public",
    "policy": "read-only",
    "cache": "90-days",
    "cdn": true
  }
}
```

### 4.2 生命周期管理

| 桶名称 | 清理策略 | 保留时间 |
|--------|---------|---------|
| achievement-files | 手动删除 | 永久保存（直到记录删除） |
| wiki-assets | 异步清理 | 随文档删除 |
| temp-uploads | 自动清理 | 24 小时 |
| project-covers | 手动删除 | 永久保存（直到项目删除） |
| knowledge-covers | 手动删除 | 永久保存（直到记录删除） |
| user-avatars | 更新覆盖 | 永久保存（更新时覆盖旧版） |

### 4.3 存储优化建议

1. **图片处理流程**
    - 上传时自动生成多尺寸缩略图
    - 使用 WebP 格式减小体积（兼容性回退 JPG）
    - 大图启用渐进式加载

2. **CDN 加速**
    - `user-avatars` 全部走 CDN
    - `project-covers` 公开项目走 CDN
    - `wiki-assets` 中的图片启用 CDN

3. **版本控制**
    - `achievement-files` 启用版本控制
    - 其他桶直接覆盖（保留备份策略）

4. **跨域配置**
    - 所有桶配置 CORS，允许前端直传
    - 生成临时签名 URL（有效期 1 小时）

5. **监控告警**
    - 监控各桶的存储容量
    - 监控 temp-uploads 的清理效率
    - 异常上传频率告警

---

## 5. URL 格式规范

### 5.1 内部存储格式（object_key）
```
achievement-files: project-{id}/achievement-{id}/v{version}_{filename}
wiki-assets: project-{id}/images/{doc_id}/{timestamp}_{filename}
temp-uploads: user-{id}/{session_id}/{uuid}_{filename}
project-covers: original/project-{id}.{ext}
knowledge-covers: {type}/{id}/cover.jpg
user-avatars: thumbnail/user-{id}_{size}.jpg
```

### 5.2 访问 URL 格式
```
直接访问：http(s)://{minio-host}/{bucket}/{object_key}
签名访问：http(s)://{minio-host}/{bucket}/{object_key}?X-Amz-Algorithm=...
CDN 访问：http(s)://{cdn-host}/{bucket}/{object_key}
```

### 5.3 数据库存储建议
```json
{
  "minio_url": "https://minio.example.com/user-avatars/thumbnail/user-123_64.jpg",
  "cdn_url": "https://cdn.example.com/user-avatars/thumbnail/user-123_64.jpg",
  "sizes": {
    "32": "https://cdn.example.com/user-avatars/thumbnail/user-123_32.jpg",
    "64": "https://cdn.example.com/user-avatars/thumbnail/user-123_64.jpg",
    "128": "https://cdn.example.com/user-avatars/thumbnail/user-123_128.jpg",
    "256": "https://cdn.example.com/user-avatars/thumbnail/user-123_256.jpg"
  }
}
```