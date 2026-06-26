# uni-app 小程序启动说明

## 环境

- HBuilderX（推荐）或 `@dcloudio/vite-plugin-uni` CLI
- 微信开发者工具
- 后端 http://127.0.0.1:8080 已启动

## 一、微信开发者工具必做设置

打开微信开发者工具 → 右上角 **详情** → **本地设置**，勾选：

- **不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书**

否则访问 `http://127.0.0.1:8080` 会被拦截。

### 关于控制台大量 WebSocket / 游客模式报错

以下日志 **可忽略**，不影响毕设演示功能：

| 日志 | 说明 |
|------|------|
| `SharedArrayBuffer` 警告 | Chrome 内核提示，与业务无关 |
| `无 AppID 关联` / `游客模式` | 未填小程序 AppID 时的正常提示 |
| `webapi_getwxaasyncsecinfo:fail` | 游客模式下微信 SDK 模拟返回 |
| `开发模式下日志通道建立 socket 连接失败` | uni-app 热更新调试通道，模拟器常连不上 |
| `closeSocket:fail ... 1006` | 同上，调试 WebSocket 关闭失败 |

如需消除「游客模式」提示：在微信公众平台申请测试 AppID，填入 `manifest.json` → `mp-weixin.appid`。

## 二、HBuilderX 运行

1. 启动后端：`cd server && mvn spring-boot:run`
2. HBuilderX 打开 `miniapp/` 目录
3. **运行 → 运行到小程序模拟器 → 微信开发者工具**
4. 登录页使用演示账号：
   - 企业：`enterprise` / `123456`
   - 技术：`technician` / `123456`

调度员、审核员请使用 Web 中心管理端。

## 三、API 地址

默认配置见 `api/request.js`：

```js
const BASE_URL = 'http://127.0.0.1:8080/api'
```

- **模拟器**：保持 `127.0.0.1` 即可
- **真机预览**：改为电脑局域网 IP，如 `http://192.168.1.100:8080/api`，且手机与电脑同一 WiFi

## 四、CLI 运行（可选）

```bash
cd miniapp
npm install
npx uni -p mp-weixin
```

编译产物在 `dist/dev/mp-weixin/`，用微信开发者工具导入该目录。

## 五、登录失败排查

1. 后端是否在 8080 端口运行（浏览器访问 http://127.0.0.1:8080/doc.html 验证）
2. 是否勾选「不校验合法域名」
3. 控制台若只有 WebSocket 报错、登录能进首页，说明接口正常

## 六、S7 待办同步说明

- 首页待办与 Web 共用 `GET /api/common/dashboard`
- 点击待办自动跳转对应小程序页（`utils/nav.js` 路由映射）
- 支持下拉刷新；从业务页返回首页时会自动刷新待办
- 企业端覆盖：需求 / 评估 / 调度 / 反馈 / 档案（简报）
- 技术端覆盖：调度 / 反馈 全部待办页
