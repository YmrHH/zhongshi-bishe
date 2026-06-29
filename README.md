# 广州生产力促进中心中试服务管理系统

毕业设计代码仓库（Web + 小程序 + 后端）

## 目录结构

```
server/     Spring Boot 3 + MyBatis-Plus + JWT
web/        Vue 3 + Vite + Element Plus
miniapp/    uni-app（企业/技术人员移动端）
原型图/     界面原型、页面清单、测试用例
分工/       三角色分工文档（总体规划 / 代码编写 / 系统测试）
指南规范/   开题方案、UML 规范等
```

## 快速启动（S0—S7 三端主链已就绪，S8 见 `分工/` 测试与规划文档）

### 1. 后端

```bash
cd server
mvn spring-boot:run
```

默认 H2 文件数据库，无需 MySQL。接口文档：http://localhost:8080/doc.html

### 2. Web

```bash
cd web
npm install
npm run dev
```

访问 http://localhost:5173

### 3. 小程序

用 HBuilderX 打开 `miniapp/` 运行到微信开发者工具；详见 [`miniapp/README.md`](miniapp/README.md)

```bash
cd miniapp
npm install
npm run dev:mp-weixin
```

## 演示账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 中试调度员 | dispatcher | 123456 |
| 中试审核员 | auditor | 123456 |
| 企业项目负责人 | enterprise | 123456 |
| 中试技术人员 | technician | 123456 |

## 开发文档（三角色分工）

| 文档 | 定位 |
|------|------|
| [`分工/项目总体规划.md`](分工/项目总体规划.md) | **唯一规划文档**（阶段节奏、业务修订专章、质量门禁、论文答辩排期；禁止另建规划稿） |
| [`分工/代码编写文档.md`](分工/代码编写文档.md) | **编码施工图**（API、状态机、三端实现、缺陷修复） |
| [`分工/系统测试文档.md`](分工/系统测试文档.md) | **测试验收**（用例、缺陷清单、E2E 记录） |
| [`原型图/页面清单.md`](原型图/页面清单.md) | 79 页实现追踪 |

当前进度：**S8 测试与论文**（[`业务流程测试用例.md`](原型图/业务流程测试用例.md)、[`答辩演示脚本.md`](原型图/答辩演示脚本.md) 已就绪；E2E 执行记录与论文正文待完成）。
