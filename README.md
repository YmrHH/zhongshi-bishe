# 广州生产力促进中心中试服务管理系统

毕业设计代码仓库（Web + 小程序 + 后端）

## 目录结构

```
server/     Spring Boot 3 + MyBatis-Plus + JWT
web/        Vue 3 + Vite + Element Plus
miniapp/    uni-app（企业/技术人员移动端）
原型图/     界面原型与《系统开发实施计划.md》
指南规范/   开题方案、UML 规范等
```

## 快速启动（S0—S2 需求+评估模块已就绪）

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

用 HBuilderX 打开 `miniapp/`，或 CLI：`npm install && npx uni -p h5`

## 演示账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 中试调度员 | dispatcher | 123456 |
| 中试审核员 | auditor | 123456 |
| 企业项目负责人 | enterprise | 123456 |
| 中试技术人员 | technician | 123456 |

## 开发计划

详见 [`原型图/系统开发实施计划.md`](原型图/系统开发实施计划.md)。当前进度：**S2 评估模块完成**，下一步 **S3 调度模块**。
