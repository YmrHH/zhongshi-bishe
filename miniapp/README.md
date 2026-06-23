# uni-app 小程序启动说明

## 环境

- HBuilderX（推荐）或 `@dcloudio/vite-plugin-uni` CLI
- 微信开发者工具（真机预览时）
- 后端 http://localhost:8080 已启动

## HBuilderX 运行

1. 用 HBuilderX 打开 `miniapp/` 目录
2. 运行 → 运行到浏览器 / 运行到小程序模拟器 → 微信开发者工具
3. 修改 `api/request.js` 中 `BASE_URL` 为可访问的后端地址

## CLI 运行（需先 npm install）

```bash
cd miniapp
npm install
npx uni -p mp-weixin
```

## 演示账号

- 企业：`enterprise` / `123456` → 企业首页
- 技术人员：`technician` / `123456` → 技术人员首页

调度员、审核员请使用 Web 中心管理端。
