<p align="center">
  <h1 align="center">智研平台 (ZhiyanPlatform)</h1>
  <p align="center">高校科研团队协作与成果管理平台</p>
</p>

<p align="center">
  <a href="#核心特性">核心特性</a> •
  <a href="#技术架构">技术架构</a> •
  <a href="#快速开始">快速开始</a> •
  <a href="#系统架构">系统架构</a> •
  <a href="#功能模块">功能模块</a> •
  <a href="#开发指南">开发指南</a> •
  <a href="#贡献指南">贡献指南</a> •
  <a href="#许可证">许可证</a>
</p>

---

## 📖 项目简介

**智研平台 (ZhiyanPlatform)** 是一个专为高校科研团队打造的**一体化、规范化、智能化**协作与知识管理解决方案。平台采用微服务架构，整合了项目管理、任务协作、成果归档、知识库、AI辅助等核心功能,旨在解决科研团队在协作过程中的信息碎片化、成果管理无序、流程效率低下等痛点。

### 🎯 项目愿景

成为高校科研团队的首选数字化协作中枢，通过技术手段提升科研创新效率，让知识的生产、沉淀与传承更加高效、有序。

### 👥 目标用户

- **导师/教授 (PIs)**: 宏观掌控多个项目的进度、成果产出和团队成员表现
- **团队负责人**: 负责具体项目的任务分解、分配和日常协作推动
- **研究生/本科生**: 项目的主要执行者，需要清晰的任务指引和便捷的协作工具
- **科研管理员**: 负责团队人员管理、项目资料归档与机构报告

---

## ✨ 核心特性

### 🔐 用户与权限管理
- **多种登录方式**: 邮箱密码登录、OAuth2.0第三方登录（GitHub等）
- **精细化权限控制**: 基于RBAC的角色权限系统，支持系统级和项目级权限
- **组织架构管理**: 支持多层级组织（学校-学院-实验室），适配高校行政架构
- **用户画像**: 研究方向标签、学术成果关联、个性化主页

### 📊 项目与任务管理
- **项目全生命周期管理**: 创建、编辑、状态流转、归档
- **可视化项目仪表盘**: 任务完成率、成员负载、进度趋势、里程碑时间线
- **任务协作**: Kanban看板、任务分配、状态跟踪、审核流程
- **项目广场**: 公开项目展示、申请加入、团队组建
- **讨论区**: 项目内沟通，支持Markdown格式和附件上传

### 📚 成果与知识管理
- **多类型成果管理**: 论文、专利、代码、数据集、模型、报告
- **成果状态流转**: 草稿 → 评审中 → 已发布
- **统一文件存储**: 基于MinIO的对象存储，支持版本控制和断点续传
- **Wiki知识库**: Markdown编辑器、文档树状结构、版本历史、多人协同编辑
- **全局智能搜索**: 基于Elasticsearch的全文检索

### 🤖 AI智能助手
- **AI辅助摘要生成**: 自动生成论文摘要和关键词
- **RAG智能问答**: 基于项目知识库的检索增强生成
- **实验数据分析**: 支持.csv、.xlsx、.mat等科研数据格式解析
- **多模式支持**: 论文模式、研究模式，提供专业领域回答

### 📝 操作日志与审计
- **全流程操作记录**: 登录、项目操作、任务操作、成果操作、Wiki操作
- **日志查询导出**: 支持条件筛选和Excel导出
- **合规性保障**: 满足科研数据"可审计、可追溯"要求

---

## 🏗️ 技术架构

### 核心技术栈

#### 后端框架
- **Spring Boot 3.2.4**: 微服务开发基础框架
- **Spring Cloud 2023.0.0**: 微服务治理
- **Spring Cloud Alibaba 2023.0.1.0**: 阿里巴巴微服务组件
- **Spring Security 6.2.4**: 安全认证与授权
- **Java 21**: 最新LTS版本

#### 微服务组件
- **Nacos**: 服务注册与配置中心
- **Gateway**: API网关与路由
- **Sentinel**: 流量控制与熔断降级
- **Seata 2.0.0**: 分布式事务
- **OpenFeign**: 服务间调用

#### 数据存储
- **MySQL 8.2.0**: 关系型数据库，存储结构化数据
- **MongoDB 4.11.1**: 文档数据库，存储Wiki内容和版本历史
- **Redis**: 缓存、分布式锁、验证码存储
- **Elasticsearch 9.1.4**: 全文检索引擎
- **MinIO 8.5.17**: 对象存储服务

#### 消息队列
- **RabbitMQ**: 异步消息处理、事件驱动

#### AI集成
- **LangChain4j 0.29.1**: AI应用框架
- **OpenAI Java Client 0.18.2**: OpenAI API集成

#### 其他核心依赖
- **Knife4j 4.4.0**: API文档（Swagger增强）
- **JWT (auth0)**: 无状态身份认证
- **Redisson 3.52.0**: Redis客户端与分布式工具
- **MapStruct-Plus 1.5.0**: 对象映射
- **Hutool 5.8.25**: Java工具类库
- **Kotlin 1.9.25**: 部分模块使用Kotlin开发

### 微服务模块

```
zhiyan-backend/
├── zhiyan-auth/          # 认证授权服务
├── zhiyan-gateway/       # API网关
├── zhiyan-nacos/         # 配置中心
├── zhiyan-modules/       # 业务模块
│   ├── zhiyan-project/   # 项目与任务管理
│   ├── zhiyan-knowledge/ # 成果管理
│   ├── zhiyan-wiki/      # Wiki知识库
│   ├── zhiyan-activelog/ # 操作日志
│   └── zhiyan-message/   # 消息通知
├── zhiyan-ai/            # AI服务
├── zhiyan-monitor/       # 监控服务
├── zhiyan-common/        # 公共模块
│   ├── zhiyan-common-basic/        # 基础工具
│   ├── zhiyan-common-security/     # 安全模块
│   ├── zhiyan-common-redis/        # Redis模块
│   ├── zhiyan-common-elasticsearch/# ES模块
│   ├── zhiyan-common-oss/          # 对象存储
│   ├── zhiyan-common-swagger/      # API文档
│   ├── zhiyan-common-sentinel/     # 流控模块
│   ├── zhiyan-common-seata/        # 分布式事务
│   ├── zhiyan-common-log/          # 日志模块
│   ├── zhiyan-common-idempotent/   # 幂等性
│   ├── zhiyan-common-excel/        # Excel处理
│   └── zhiyan-common-oauth/        # OAuth2.0
└── zhiyan-api/           # API接口定义
```

---

## 🚀 快速开始

### 环境要求

- **JDK**: 21+
- **Maven**: 3.8+
- **MySQL**: 8.0+
- **MongoDB**: 4.4+
- **Redis**: 6.0+
- **Elasticsearch**: 9.x
- **MinIO**: 最新版
- **RabbitMQ**: 3.11+
- **Nacos**: 2.x

### 本地开发环境搭建

#### 1. 克隆项目

```bash
git clone https://github.com/ergou10086/ZhiyanPlatform.git
cd ZhiyanPlatform
```

#### 2. 初始化数据库

执行 `sql/` 目录下的SQL脚本：

```bash
# MySQL数据库初始化
cd sql/zhiyan-mysql-service
# 依次执行各服务的SQL脚本

# MongoDB初始化
cd sql/zhiyan-mongodb
# 执行MongoDB初始化脚本

# Elasticsearch索引初始化
cd sql/zhiyan-elasticsearch
# 执行ES索引创建脚本
```

#### 3. 配置Nacos

启动Nacos服务器，并导入配置文件（参考 `docx/项目内说明/Nacos模块配置.md`）

#### 4. 修改配置

根据实际环境修改各服务的配置文件（通过Nacos配置中心管理）：
- 数据库连接信息
- Redis连接信息
- MinIO配置
- Elasticsearch配置
- RabbitMQ配置

#### 5. 启动服务

按以下顺序启动服务：

```bash
# 1. 启动Nacos（配置中心）
cd zhiyan-nacos
mvn spring-boot:run

# 2. 启动网关
cd zhiyan-gateway
mvn spring-boot:run

# 3. 启动认证服务
cd zhiyan-auth
mvn spring-boot:run

# 4. 启动业务服务
cd zhiyan-modules/zhiyan-project
mvn spring-boot:run

# ... 启动其他业务服务
```

#### 6. 访问系统

- **API网关**: http://localhost:8080
- **API文档**: http://localhost:8080/doc.html (Knife4j)
- **Nacos控制台**: http://localhost:8848/nacos

---

## 📐 系统架构

### 微服务架构图

```
┌─────────────────────────────────────────────────────────────┐
│                         前端应用层                            │
│                    (Vue.js / React)                          │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      API Gateway (网关)                       │
│              路由、鉴权、限流、日志、监控                       │
└─────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
┌──────────────┐    ┌──────────────┐      ┌──────────────┐
│  认证授权服务  │    │  项目管理服务  │      │  成果管理服务  │
│  (Auth)      │    │  (Project)   │      │  (Knowledge) │
└──────────────┘    └──────────────┘      └──────────────┘
        │                     │                     │
        ▼                     ▼                     ▼
┌──────────────┐    ┌──────────────┐      ┌──────────────┐
│  Wiki服务     │    │  AI服务       │      │  消息服务     │
│  (Wiki)      │    │  (AI)        │      │  (Message)   │
└──────────────┘    └──────────────┘      └──────────────┘
        │                     │                     │
        └─────────────────────┼─────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      服务注册与配置中心                        │
│                         (Nacos)                              │
└─────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
┌──────────────┐    ┌──────────────┐      ┌──────────────┐
│    MySQL     │    │   MongoDB    │      │    Redis     │
└──────────────┘    └──────────────┘      └──────────────┘
        ▼                     ▼                     ▼
┌──────────────┐    ┌──────────────┐      ┌──────────────┐
│Elasticsearch │    │    MinIO     │      │  RabbitMQ    │
└──────────────┘    └──────────────┘      └──────────────┘
```

### 数据存储策略

| 存储类型 | 负责内容 | 理由 |
|---------|---------|------|
| MySQL | 用户、项目、任务、成果元数据 | 强关系型、事务保证 |
| MongoDB | Wiki内容、版本历史 | 灵活、写入性能好 |
| Elasticsearch | 全文索引 | 专业搜索引擎 |
| MinIO | 文件、图片、附件 | 对象存储 |
| Redis | 缓存、会话、验证码 | 高性能读写 |

---

## 📦 功能模块

### 1. 用户与权限服务 (zhiyan-auth)

**核心功能**:
- 用户注册/登录（邮箱验证码、OAuth2.0）
- JWT令牌管理（Access Token + Refresh Token）
- 基于RBAC的权限控制
- 组织架构管理
- 用户画像与研究方向标签

**技术实现**:
- Spring Security + JWT
- Redis（验证码、Token黑名单）
- MySQL（用户、角色、权限）

### 2. 项目与任务管理服务 (zhiyan-project)

**核心功能**:
- 项目创建、编辑、状态管理
- 任务分配、状态跟踪、审核流程
- 项目仪表盘（可视化数据分析）
- 项目广场（公开项目展示）
- 团队成员管理
- 项目讨论区

**技术实现**:
- Spring Boot + MyBatis
- MySQL（项目、任务、成员）
- RabbitMQ（任务通知）
- Redis（缓存）

### 3. 成果管理服务 (zhiyan-knowledge)

**核心功能**:
- 多类型成果管理（论文、专利、代码等）
- 成果状态流转与评审
- 文件上传/下载（支持断点续传）
- 批量操作
- 文件预览（多种格式）
- 成果与任务关联

**技术实现**:
- Spring Boot
- MySQL（成果元数据）
- MinIO（文件存储）
- Elasticsearch（全文检索）

### 4. Wiki知识库服务 (zhiyan-wiki)

**核心功能**:
- Markdown文档编辑
- 文档树状结构
- 版本历史管理
- 多人协同编辑
- 实时协作感知

**技术实现**:
- Spring Boot
- MongoDB（文档内容、版本历史）
- WebSocket（实时协作）
- MinIO（附件存储）

### 5. AI服务 (zhiyan-ai)

**核心功能**:
- AI辅助摘要生成
- RAG智能问答
- 实验数据分析
- 多模式支持（论文模式、研究模式）
- 长回合对话

**技术实现**:
- LangChain4j
- OpenAI API
- Elasticsearch（向量检索）
- 文件解析（.csv、.xlsx、.mat等）

### 6. 操作日志服务 (zhiyan-activelog)

**核心功能**:
- 全流程操作记录
- 日志查询与筛选
- 日志导出（Excel）
- 审计追溯

**技术实现**:
- Spring Boot
- MySQL（日志存储）
- AOP切面（自动记录）

### 7. 消息通知服务 (zhiyan-message)

**核心功能**:
- 站内信通知
- 邮件通知
- 事件驱动通知

**技术实现**:
- RabbitMQ（消息队列）
- Spring Mail（邮件发送）

---

## 🛠️ 开发指南

### 代码规范

- 遵循阿里巴巴Java开发手册
- 使用Lombok简化代码
- 统一使用MapStruct进行对象映射
- API接口使用RESTful风格
- 统一返回结果封装（R类）

### API文档

项目集成Knife4j，启动服务后访问：
```
http://localhost:{port}/doc.html
```

### 配置管理

所有配置通过Nacos配置中心管理，支持：
- 环境分离（dev、test、prod）
- 动态刷新
- 配置版本管理

详见：`docx/项目内说明/配置文件环境分离说明.md`

### 性能优化

项目已实施多项性能优化：
- Redis缓存策略
- 数据库索引优化
- 分页查询优化
- 异步处理
- 连接池优化

详见：`docx/项目内说明/性能优化总结.md`

---

## 📚 文档

项目文档位于 `docx/` 目录：

- **产品设计**: 产品设计文档、数据库设计文档
- **项目内说明**: 各模块配置说明、使用指南
- **请求示例文档**: API请求示例
- **ErgouTreePoint**: 复杂业务设计思路

---

## 🧪 测试

### 压力测试

项目提供JMeter压测计划，位于 `Jmeter压测计划/` 目录。

### API测试

推荐使用Apifox或Postman，可导入Knife4j生成的API文档。

---

## 🤝 贡献指南

欢迎贡献代码、提出问题和建议！

### 贡献流程

1. Fork本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交Pull Request

### 提交规范

遵循Conventional Commits规范：
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具链相关

---

## 📄 许可证

本项目采用 [Apache License 2.0](LICENSE) 开源协议。

---

## 👨‍💻 作者

**ergou10086**

- GitHub: [@ergou10086](https://github.com/ergou10086)
- 项目地址: [https://github.com/ergou10086/ZhiyanPlatform](https://github.com/ergou10086/ZhiyanPlatform)

---

## 🙏 致谢

感谢所有为本项目做出贡献的开发者！

特别感谢以下开源项目：
- Spring Boot & Spring Cloud
- Spring Cloud Alibaba
- Nacos
- Sentinel
- Elasticsearch
- MinIO
- LangChain4j

---

## 📞 联系我们

如有问题或建议，欢迎通过以下方式联系：

- 提交Issue: [GitHub Issues](https://github.com/ergou10086/ZhiyanPlatform/issues)
- 邮箱: [在此添加联系邮箱]

---

<p align="center">
  <sub>Built with ❤️ by the ZhiyanPlatform Team</sub>
</p> 
