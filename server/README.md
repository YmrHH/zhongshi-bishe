# 后端启动说明

## 环境

- JDK 17+
- Maven 3.8+（IDEA 自带亦可）

## 开发模式（默认 H2 文件库，无需安装 MySQL）

```bash
cd server
mvn spring-boot:run
```

- API：http://localhost:8080
- 接口文档：http://localhost:8080/doc.html
- H2 控制台：http://localhost:8080/h2-console

## MySQL 模式

1. 创建数据库 `zhongshi_service`
2. 启动：`mvn spring-boot:run -Dspring-boot.run.profiles=mysql`

## 演示账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 中试调度员 | dispatcher | 123456 |
| 中试审核员 | auditor | 123456 |
| 企业项目负责人 | enterprise | 123456 |
| 中试技术人员 | technician | 123456 |
