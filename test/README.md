# 自动化测试（test/）

广州生产力促进中心中试服务管理系统 API 自动化测试脚本，按模块分类组织，不与业务代码混放。

## 目录结构

```
test/
├── config/              # 环境配置（API 地址、账号）
├── common/              # 公共能力（HTTP 客户端、登录、断言、日志）
├── docs/                # 测试用例文档（按模块）
│   └── 需求管理测试用例.md
├── fixtures/            # 测试数据与样例文件
│   └── materials/       # 需求材料附件样例
├── lib/                 # 按领域封装的 API
│   ├── files/           # 文件上传
│   └── demand/          # 需求模块 API
├── scenarios/           # 业务场景脚本（可组合多步流程）
│   └── demand/          # 需求管理场景
├── runners/             # 可执行入口（npm scripts 指向此处）
├── package.json
└── README.md
```

## 前置条件

1. 后端已启动（默认 `http://localhost:8080`）
2. Node.js 18+
3. 演示账号已初始化（enterprise / dispatcher / auditor，密码 `123456`）

```bash
cd server
mvn spring-boot:run
```

## 快速开始

```bash
cd test

# 生成材料样例文件（首次运行）
npm run fixtures:init

# 运行：中试需求管理全流程
npm run demand:flow
```

**详细测试用例（手工 + 场景）：** [`docs/需求管理测试用例.md`](docs/需求管理测试用例.md)

## 需求管理全流程（demand:flow）

| 步骤 | 角色 | 操作 |
|------|------|------|
| 1 | enterprise | 创建项目 → 填写需求信息 → **随机 1~3 种材料上传** → 提交 |
| 2 | dispatcher | 需求受理登记 |
| 3 | auditor | 材料核验（齐全）→ 同意受理 |
| 4 | enterprise | 受理回执签收 |

**预期最终状态：** `status=RECEIPTED`，`stage=DEMAND`

## 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `API_BASE_URL` | `http://localhost:8080/api` | 后端 API 根路径 |
| `TEST_PASSWORD` | `123456` | 演示账号密码 |
| `TEST_USER_ENTERPRISE` | `enterprise` | 企业账号 |
| `TEST_USER_DISPATCHER` | `dispatcher` | 调度员账号 |
| `TEST_USER_AUDITOR` | `auditor` | 审核员账号 |

## 扩展

- 新增模块 API：在 `lib/<模块>/api.js` 添加封装
- 新增场景：在 `scenarios/<模块>/` 编写流程
- 新增入口：在 `runners/` 添加脚本并在 `package.json` 的 `scripts` 注册

详细测试用例与缺陷记录见 [`分工/系统测试文档.md`](../分工/系统测试文档.md)。
