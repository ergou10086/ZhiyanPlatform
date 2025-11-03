# Wiki附件管理功能说明

## 📌 功能概述

Wiki附件管理功能为Wiki页面提供了完整的文件管理能力，支持图片和普通文件的上传、下载、查询和删除。

## 🎯 核心特性

### 1. **附件类型支持**
- **IMAGE（图片）**: jpg, png, gif, webp等图片格式
- **FILE（文件）**: pdf, doc, zip, txt等普通文件格式

### 2. **功能列表**
- ✅ 单文件上传
- ✅ 批量文件上传
- ✅ 文件下载
- ✅ 附件列表查询（支持分页、筛选、排序）
- ✅ 附件详情查询
- ✅ 软删除（支持恢复）
- ✅ 物理删除（彻底删除）
- ✅ 统计信息（数量、总大小）

### 3. **存储架构**
- **MySQL**: 存储附件元数据（文件名、大小、类型、URL等）
- **MinIO**: 存储实际文件内容
- **桶名称**: `wikiassets`

## 📡 API接口说明

### 基础路径
```
/api/wiki/attachments
```

### 1. 上传附件

**单文件上传**
```http
POST /api/wiki/attachments/upload
Content-Type: multipart/form-data

Parameters:
- file: MultipartFile (必需) - 上传的文件
- wikiPageId: Long (必需) - Wiki页面ID
- projectId: Long (必需) - 项目ID
- attachmentType: String (可选) - 附件类型(IMAGE/FILE)，不指定则自动判断
- description: String (可选) - 文件描述
```

**批量上传**
```http
POST /api/wiki/attachments/upload/batch
Content-Type: multipart/form-data

Parameters:
- files: MultipartFile[] (必需) - 文件数组
- wikiPageId: Long (必需) - Wiki页面ID
- projectId: Long (必需) - 项目ID
- attachmentType: String (可选) - 附件类型
```

**响应示例**
```json
{
  "code": 200,
  "msg": "附件上传成功",
  "data": {
    "id": "1234567890",
    "wikiPageId": "100",
    "projectId": "1",
    "attachmentType": "IMAGE",
    "fileName": "example.png",
    "fileSize": 102400,
    "fileSizeFormatted": "100.00 KB",
    "fileType": "png",
    "fileUrl": "http://localhost:9000/wikiassets/project-1/images/100/20231201_example.png",
    "description": "示例图片",
    "uploadBy": "1",
    "uploadAt": "2023-12-01 12:34:56"
  }
}
```

### 2. 查询附件

**获取页面所有附件**
```http
GET /api/wiki/attachments/page/{wikiPageId}
```

**获取页面图片列表**
```http
GET /api/wiki/attachments/page/{wikiPageId}/images
```

**获取页面文件列表**
```http
GET /api/wiki/attachments/page/{wikiPageId}/files
```

**分页查询项目附件**
```http
GET /api/wiki/attachments/project/{projectId}?page=0&size=20&attachmentType=IMAGE&fileName=test&sortBy=uploadAt&sortDirection=DESC
```

**获取附件详情**
```http
GET /api/wiki/attachments/{attachmentId}
```

### 3. 下载附件

```http
GET /api/wiki/attachments/{attachmentId}/download
```

浏览器会自动下载文件，文件名为原始文件名。

### 4. 删除附件

**软删除（可恢复）**
```http
DELETE /api/wiki/attachments/{attachmentId}
```

**物理删除（不可恢复）**
```http
DELETE /api/wiki/attachments/{attachmentId}/permanent
```

**删除页面所有附件**
```http
DELETE /api/wiki/attachments/page/{wikiPageId}
```

### 5. 统计信息

```http
GET /api/wiki/attachments/project/{projectId}/stats
```

**响应示例**
```json
{
  "code": 200,
  "data": {
    "totalCount": 150,
    "totalSize": 52428800,
    "totalSizeFormatted": "50.00 MB"
  }
}
```

## 🔐 权限控制

所有接口都需要登录认证，具体权限要求如下：

| 操作 | 权限要求 |
|-----|---------|
| 上传附件 | 项目成员 |
| 查看附件 | Wiki页面访问权限 |
| 下载附件 | Wiki页面访问权限 |
| 删除附件 | Wiki页面编辑权限 |
| 物理删除 | Wiki页面删除权限 |

## 💾 数据库设计

### wiki_attachment 表结构

```sql
CREATE TABLE `wiki_attachment` (
    `id` BIGINT NOT NULL COMMENT '附件唯一标识（雪花ID）',
    `wiki_page_id` BIGINT NOT NULL COMMENT '所属Wiki页面ID',
    `project_id` BIGINT NOT NULL COMMENT '所属项目ID',
    `attachment_type` VARCHAR(20) NOT NULL COMMENT '附件类型',
    `file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
    `file_type` VARCHAR(50) COMMENT '文件类型/扩展名',
    `bucket_name` VARCHAR(100) NOT NULL COMMENT 'MinIO桶名',
    `object_key` VARCHAR(500) NOT NULL COMMENT 'MinIO对象键',
    `file_url` VARCHAR(1000) NOT NULL COMMENT '完整访问URL',
    `description` VARCHAR(500) COMMENT '文件描述',
    `upload_by` BIGINT NOT NULL COMMENT '上传者ID',
    `upload_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `deleted_at` TIMESTAMP NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 📝 使用示例

### 前端上传示例（Vue.js）

```javascript
// 单文件上传
async function uploadAttachment(file, wikiPageId, projectId) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('wikiPageId', wikiPageId);
  formData.append('projectId', projectId);
  formData.append('description', '这是一个示例文件');
  
  const response = await axios.post('/api/wiki/attachments/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
  
  return response.data.data;
}

// 批量上传
async function uploadMultipleFiles(files, wikiPageId, projectId) {
  const formData = new FormData();
  files.forEach(file => {
    formData.append('files', file);
  });
  formData.append('wikiPageId', wikiPageId);
  formData.append('projectId', projectId);
  
  const response = await axios.post('/api/wiki/attachments/upload/batch', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
  
  return response.data.data;
}

// 获取页面附件
async function getPageAttachments(wikiPageId) {
  const response = await axios.get(`/api/wiki/attachments/page/${wikiPageId}`);
  return response.data.data;
}

// 下载附件
function downloadAttachment(attachmentId, fileName) {
  const url = `/api/wiki/attachments/${attachmentId}/download`;
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  link.click();
}

// 删除附件
async function deleteAttachment(attachmentId) {
  const response = await axios.delete(`/api/wiki/attachments/${attachmentId}`);
  return response.data;
}
```

### Markdown编辑器集成

```javascript
// 图片上传后，插入Markdown语法
const result = await uploadAttachment(file, wikiPageId, projectId);
const markdownImage = `![${result.fileName}](${result.fileUrl})`;
// 将markdownImage插入到编辑器光标位置

// 附件上传后，插入下载链接
const result = await uploadAttachment(file, wikiPageId, projectId);
const markdownLink = `[📎 ${result.fileName}](${result.fileUrl})`;
```

## 🎨 MinIO存储路径规则

附件在MinIO中的存储路径格式：
```
{bucket}/project-{projectId}/{type}/{wikiPageId}/{timestamp}_{filename}

示例：
wikiassets/project-1/images/100/20231201123456_example.png
wikiassets/project-1/attachments/100/20231201123456_document.pdf
```

## ⚙️ 配置说明

### 文件大小限制
在 `application.yml` 中配置：
```yaml
minio:
  upload:
    max-image-size: 5242880  # 5MB
    max-file-size: 52428800   # 50MB
```

### 允许的文件类型
```yaml
minio:
  upload:
    allowed-image-types:
      - jpg
      - jpeg
      - png
      - gif
      - webp
    allowed-file-types:
      - "*"  # 允许所有类型，或指定具体类型
```

## 🐛 常见问题

### 1. 上传失败
- 检查文件大小是否超过限制
- 检查文件类型是否被允许
- 检查MinIO服务是否正常运行
- 检查网络连接

### 2. 下载失败
- 检查附件是否存在
- 检查用户是否有访问权限
- 检查MinIO中的文件是否存在

### 3. 权限问题
- 确保用户已登录
- 确保用户是项目成员
- 确保用户对Wiki页面有相应权限

## 📊 性能优化建议

1. **图片压缩**: 前端上传前对图片进行压缩
2. **缩略图**: 为图片生成缩略图，提高加载速度
3. **CDN加速**: 将MinIO配置CDN加速访问
4. **懒加载**: 附件列表使用虚拟滚动或分页加载
5. **缓存**: 对附件列表进行适当缓存

## 🔄 未来扩展

- [ ] 图片裁剪和编辑
- [ ] 视频文件支持
- [ ] 文件预览（PDF、Office文档）
- [ ] 图片水印
- [ ] 文件病毒扫描
- [ ] 附件版本管理
- [ ] 图床功能（外链分享）

## 📞 技术支持

如有问题，请联系开发团队或提交Issue。


