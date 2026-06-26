# -*- coding: utf-8 -*-
"""Generate extended 页面清单.md with route/API columns from HTML filenames."""
import re
from pathlib import Path

ROOT = Path(__file__).parent
HTML_FILES = sorted(ROOT.rglob("*.html"))
HTML_FILES = [f for f in HTML_FILES if f.name != "index.html"]

MODULE_MAP = {
    "需求": "demand",
    "评估": "evaluation",
    "调度": "dispatch",
    "反馈": "feedback",
    "档案": "archive",
    "公共": "common",
}

PAGE_SLUG = {
    "登录页": "login",
    "管理工作台首页": "home",
    "企业门户首页": "home",
    "技术人员首页": "home",
    "首页": "home",
    "需求预览确认页": "preview",
    "需求填报页": "submit",
    "需求受理工作台": "workbench",
    "退回意见页": "reject",
    "材料补充页": "supplement",
    "材料核验页": "verify",
    "受理结果录入页": "accept-result",
    "受理回执签收页": "receipt",
    "需求受理归档页": "archive",
    "受理进度详情页": "progress",
    "评估前置核查页": "precheck",
    "条件评估页": "condition",
    "条件整改通知页": "rectify-notice",
    "条件材料补充页": "condition-supplement",
    "资源核定页": "resource",
    "可行性审查页": "feasibility",
    "评估材料补充页": "eval-supplement",
    "评估结论页": "conclusion",
    "评估结论签收页": "conclusion-receipt",
    "评估意见反馈页": "feedback",
    "评估归档页": "archive",
    "评估结论详情页": "conclusion-detail",
    "资源匹配页": "match",
    "任务派发页": "assign",
    "派单通知页": "assign-notice",
    "任务接收页": "receive",
    "任务确认签收页": "confirm",
    "进度填报页": "progress-report",
    "进度通报督办页": "supervise",
    "异常重派页": "reassign",
    "执行结果确认页": "exec-confirm",
    "进度查看页": "progress-view",
    "调度归档页": "archive",
    "结果提交页": "submit",
    "数据校验页": "validate",
    "报告审核页": "audit",
    "结果修改页": "modify",
    "复核确认页": "review",
    "报告归档页": "report-archive",
    "复核结果通知页": "review-notice",
    "复核意见反馈页": "review-feedback",
    "反馈审核页": "feedback-audit",
    "复核结果详情页": "review-detail",
    "报告结案归档页": "case-archive",
    "台账维护页": "ledger",
    "档案确认页": "confirm",
    "结案资料归集页": "collect",
    "周期统计页": "cycle-stats",
    "成功率分析页": "success-rate",
    "简报生成页": "brief-generate",
    "简报审核页": "brief-audit",
    "简报查看页": "brief-view",
    "档案归档页": "archive",
}

API_MAP = {
    "login": "POST /api/auth/login",
    "home": "GET /api/common/dashboard",
    "preview": "GET/POST /api/projects/{id}/demand/preview",
    "submit": "POST /api/projects/{id}/demand/submit",
    "workbench": "GET /api/demand/todos; POST /api/projects/{id}/demand/accept",
    "reject": "POST /api/projects/{id}/demand/reject",
    "supplement": "POST /api/projects/{id}/demand/supplement",
    "verify": "POST /api/projects/{id}/demand/verify",
    "accept-result": "POST /api/projects/{id}/demand/accept-result",
    "receipt": "POST /api/projects/{id}/demand/receipt",
    "archive": "POST /api/projects/{id}/{module}/archive",
    "progress": "GET /api/projects/{id}/demand/progress",
    "precheck": "POST /api/projects/{id}/evaluation/precheck",
    "condition": "POST /api/projects/{id}/evaluation/condition",
    "rectify-notice": "POST /api/projects/{id}/evaluation/rectify-notice",
    "condition-supplement": "POST /api/projects/{id}/evaluation/condition-supplement",
    "resource": "POST /api/projects/{id}/evaluation/resource",
    "feasibility": "POST /api/projects/{id}/evaluation/feasibility",
    "eval-supplement": "POST /api/projects/{id}/evaluation/supplement",
    "conclusion": "POST /api/projects/{id}/evaluation/conclusion",
    "conclusion-receipt": "POST /api/projects/{id}/evaluation/receipt",
    "feedback": "POST /api/projects/{id}/evaluation/feedback",
    "conclusion-detail": "GET /api/projects/{id}/evaluation/detail",
    "match": "POST /api/projects/{id}/dispatch/match",
    "assign": "POST /api/projects/{id}/dispatch/assign",
    "assign-notice": "POST /api/projects/{id}/dispatch/assign-notice",
    "receive": "POST /api/tasks/{id}/receive",
    "confirm": "POST /api/tasks/{id}/confirm",
    "progress-report": "POST /api/tasks/{id}/progress",
    "supervise": "POST /api/projects/{id}/dispatch/supervise",
    "reassign": "POST /api/projects/{id}/dispatch/reassign",
    "exec-confirm": "POST /api/projects/{id}/dispatch/exec-confirm",
    "progress-view": "GET /api/projects/{id}/dispatch/progress",
    "validate": "POST /api/projects/{id}/feedback/validate",
    "audit": "POST /api/projects/{id}/feedback/audit",
    "modify": "POST /api/projects/{id}/feedback/modify",
    "review": "POST /api/projects/{id}/feedback/review",
    "report-archive": "POST /api/projects/{id}/feedback/report-archive",
    "review-notice": "POST /api/projects/{id}/feedback/review-notice",
    "review-feedback": "POST /api/projects/{id}/feedback/review-feedback",
    "feedback-audit": "POST /api/projects/{id}/feedback/feedback-audit",
    "review-detail": "GET /api/projects/{id}/feedback/review-detail",
    "case-archive": "POST /api/projects/{id}/feedback/case-archive",
    "ledger": "GET/PUT /api/archive/ledger",
    "collect": "POST /api/projects/{id}/archive/collect",
    "cycle-stats": "GET /api/archive/cycle-stats",
    "success-rate": "GET /api/archive/success-rate",
    "brief-generate": "POST /api/archive/brief/generate",
    "brief-audit": "POST /api/archive/brief/audit",
    "brief-view": "GET /api/archive/brief/{id}",
}

ROLE_PORTAL = {
    "Web-公共": ("Web", "公共用户 / 公共", "/login"),
    "Web-中心-公共": ("Web", "中试调度员 / 中心管理端", "/center/home"),
    "Web-企业-公共": ("Web", "企业项目负责人 / 企业门户", "/enterprise/home"),
    "Web-技术-公共": ("Web", "中试技术人员 / 技术人员门户", "/technician/home"),
    "Web-中心-调度": ("Web", "中试调度员 / 中心管理端", "/center/dispatch"),
    "Web-中心-审核": ("Web", "中试审核员 / 中心管理端", "/center/audit"),
    "Web-企业": ("Web", "企业项目负责人 / 企业门户", "/enterprise"),
    "Web-技术": ("Web", "中试技术人员 / 技术人员门户", "/technician"),
    "小程序-企业-公共": ("小程序", "企业项目负责人 / 企业门户", "pages/enterprise/home"),
    "小程序-技术-公共": ("小程序", "中试技术人员 / 技术人员门户", "pages/technician/home"),
    "小程序-企业": ("小程序", "企业项目负责人 / 企业门户", "pages/enterprise"),
    "小程序-技术": ("小程序", "中试技术人员 / 技术人员门户", "pages/technician"),
}

MODULE_CN = {
    "需求": "中试需求管理",
    "评估": "中试评估管理",
    "调度": "中试调度管理",
    "反馈": "中试反馈管理",
    "档案": "中试档案管理",
    "公共": "公共",
}

ACTIVITY = {
    "需求预览确认页": "需求信息确认",
    "需求填报页": "中试需求提交",
    "需求受理工作台": "需求受理登记",
    "退回意见页": "需求退回通知",
    "材料补充页": "需求材料补充",
    "材料核验页": "受理材料核验",
    "受理结果录入页": "受理结果通知",
    "受理回执签收页": "受理回执签收",
    "需求受理归档页": "受理信息归档",
    "受理进度详情页": "查看受理进度",
    "评估前置核查页": "评估前置核查",
    "条件评估页": "中试条件评估",
    "条件整改通知页": "条件整改通知",
    "条件材料补充页": "条件材料补充",
    "资源核定页": "资源需求核定",
    "可行性审查页": "技术可行性审",
    "评估材料补充页": "评估材料补充",
    "评估结论页": "评估结论出具",
    "评估结论签收页": "评估结果签收",
    "评估意见反馈页": "评估意见反馈",
    "评估归档页": "评估信息归档",
    "评估结论详情页": "查看评估结论",
    "资源匹配页": "中试资源匹配",
    "任务派发页": "中试任务派发",
    "派单通知页": "派单结果通知",
    "任务接收页": "接收任务执行",
    "任务确认签收页": "任务接收确认",
    "进度填报页": "填报执行进度",
    "进度通报督办页": "执行进度通报",
    "异常重派页": "异常任务重派",
    "执行结果确认页": "执行结果确认",
    "进度查看页": "查看执行进度",
    "调度归档页": "调度信息归档",
    "结果提交页": "试验结果提交",
    "数据校验页": "试验数据校验",
    "报告审核页": "中试报告审核",
    "结果修改页": "试验结果修改",
    "复核确认页": "结果复核确认",
    "报告归档页": "中试报告归档",
    "复核结果通知页": "复核结果通知",
    "复核意见反馈页": "复核意见反馈",
    "反馈审核页": "反馈结果审核",
    "复核结果详情页": "查看复核结果",
    "报告结案归档页": "报告信息归档",
    "台账维护页": "项目台账维护",
    "档案确认页": "档案信息确认",
    "结案资料归集页": "结案资料归集",
    "周期统计页": "中试周期统计",
    "成功率分析页": "成功率分析",
    "简报生成页": "服务简报生成",
    "简报审核页": "简报内容审核",
    "简报查看页": "查看服务简报",
    "档案归档页": "档案信息归档",
    "登录页": "公共页面",
    "管理工作台首页": "公共页面",
    "企业门户首页": "公共页面",
    "技术人员首页": "公共页面",
    "首页": "公共页面",
}


def parse_filename(name: str):
    base = name.replace(".html", "")
    m = re.match(r"^(Web|小程序)-(.+)-([^-]+)-(.+)$", base)
    if not m:
        return None
    platform, part2, mod_key, page_title = m.groups()
    prefix = f"{platform}-{part2}"
    if mod_key in MODULE_MAP:
        module_cn = MODULE_CN.get(mod_key, mod_key)
    else:
        module_cn = "公共"
        page_title = mod_key + "-" + page_title if mod_key else page_title
    return platform, prefix, mod_key, page_title, module_cn, base


def route_for(platform, prefix, mod_key, page_title):
    slug = PAGE_SLUG.get(page_title, page_title)
    mod_en = MODULE_MAP.get(mod_key, "common")
    for key, (_, _, base) in ROLE_PORTAL.items():
        if prefix.startswith(key) or prefix == key:
            portal_base = base
            break
    else:
        portal_base = "/"
    if page_title in ("登录页",):
        return "/login"
    if mod_key == "公共" or page_title in ("管理工作台首页", "企业门户首页", "技术人员首页", "首页"):
        return portal_base if portal_base.endswith("home") else portal_base + "/home"
    if platform == "Web":
        if prefix.startswith("Web-中心-调度"):
            return f"/center/dispatch/{mod_en}/{slug}"
        if prefix.startswith("Web-中心-审核"):
            return f"/center/audit/{mod_en}/{slug}"
        if prefix.startswith("Web-企业"):
            return f"/enterprise/{mod_en}/{slug}"
        if prefix.startswith("Web-技术"):
            return f"/technician/{mod_en}/{slug}"
    else:
        if prefix.startswith("小程序-企业"):
            return f"pages/enterprise/{mod_en}/{slug}"
        if prefix.startswith("小程序-技术"):
            return f"pages/technician/{mod_en}/{slug}"
    return f"/{mod_en}/{slug}"


def api_for(page_title, mod_key):
    slug = PAGE_SLUG.get(page_title, "unknown")
    api = API_MAP.get(slug, f"GET/POST /api/{MODULE_MAP.get(mod_key, 'common')}/{slug}")
    if slug == "archive" and mod_key:
        api = f"POST /api/projects/{{id}}/{MODULE_MAP.get(mod_key, 'archive')}/archive"
    return api


rows = []
for i, html in enumerate(sorted(HTML_FILES, key=lambda p: str(p)), 1):
    parsed = parse_filename(html.name)
    if not parsed:
        continue
    platform, prefix, mod_key, page_title, module_cn, page_id = parsed
    # fix page_title for 公共 pages
    if mod_key == "公共" and "-" in page_title:
        parts = html.stem.split("-")
        page_title = parts[-1] if parts[-1].endswith("页") or parts[-1] == "首页" else parts[-1]
    role = ROLE_PORTAL.get(prefix, ROLE_PORTAL.get(prefix.split("-")[0] + "-" + prefix.split("-")[1], ("", "", "")))[1]
    if not role:
        for k, v in ROLE_PORTAL.items():
            if html.as_posix().find(k.replace("Web-", "Web/").replace("小程序-", "小程序/")) >= 0 or prefix.startswith(k):
                role = v[1]
                break
    # infer role from path
    p = html.as_posix()
    if "调度员" in p:
        role = "中试调度员 / 中心管理端"
    elif "审核员" in p:
        role = "中试审核员 / 中心管理端"
    elif "企业门户" in p or "企业端" in p:
        role = "企业项目负责人 / 企业门户"
    elif "技术人员" in p:
        role = "中试技术人员 / 技术人员门户"
    elif "00-公共" in p:
        role = "公共用户 / 公共"
    route = route_for(platform, prefix, mod_key, page_title)
    api = api_for(page_title, mod_key)
    activity = ACTIVITY.get(page_title, "公共页面")
    png = html.with_suffix(".png")
    rel_html = html.relative_to(ROOT.parent).as_posix()
    rel_png = png.relative_to(ROOT.parent).as_posix() if png.exists() else rel_html.replace(".html", ".png")
    rows.append((i, platform, role, module_cn, page_title, activity, rel_png, rel_html, "未开始", route, api, ""))

# reorder by original 页面清单 order - use fixed order from existing file
ORDER = [
    "Web-公共-登录页", "Web-中心-公共-管理工作台首页", "Web-企业-公共-企业门户首页",
    "Web-技术-公共-技术人员首页", "小程序-企业-公共-首页", "小程序-技术-公共-首页",
]
# build from html list sorted by custom key
def sort_key(html_path):
    name = html_path.stem
    try:
        idx = list(Path(ROOT).rglob("*.html"))
        names = [f.stem for f in sorted(idx, key=lambda x: (
            0 if "00-公共" in str(x) else 1,
            0 if "01-中心" in str(x) and "调度员" in str(x) else 2,
            0 if "01-中心" in str(x) and "审核员" in str(x) else 3,
            0 if "02-企业" in str(x) else 4,
            0 if "03-技术" in str(x) else 5,
            0 if "企业端" in str(x) else 6,
            0 if "技术人员端" in str(x) else 7,
            str(x)
        ))]
        return names.index(name)
    except ValueError:
        return 999

all_html = [f for f in HTML_FILES]
# Manual order matching original 页面清单
ordered_names = []
for line in Path(ROOT / "页面清单.md").read_text(encoding="utf-8").splitlines():
    m = re.search(r"`(原型图/[^`]+\.html)`", line)
    if m:
        ordered_names.append(m.group(1))

rows_out = []
for seq, rel in enumerate(ordered_names, 1):
    html_path = ROOT.parent / rel.replace("/", "\\") if "\\" in str(ROOT) else ROOT.parent / rel
    html_path = ROOT.parent / rel
    if not html_path.exists():
        html_path = ROOT / Path(rel).name
    name = html_path.stem
    parsed = parse_filename(name + ".html")
    if not parsed:
        continue
    platform, prefix, mod_key, page_title, module_cn, page_id = parsed
    p = str(html_path)
    if "调度员" in p:
        role = "中试调度员 / 中心管理端"
    elif "审核员" in p:
        role = "中试审核员 / 中心管理端"
    elif "企业门户" in p or "企业端" in p:
        role = "企业项目负责人 / 企业门户"
    elif "技术人员" in p:
        role = "中试技术人员 / 技术人员门户"
    else:
        role = "公共用户 / 公共"
    route = route_for(platform, prefix, mod_key, page_title)
    api = api_for(page_title, mod_key)
    activity = ACTIVITY.get(page_title, "公共页面")
    rel_html = rel
    rel_png = rel.replace(".html", ".png")
    rows_out.append((seq, platform, role, module_cn, page_title, activity, rel_png, rel_html, "未开始", route, api, ""))

out = """# 原型图页面清单

生成对象：广州生产力促进中心中试服务管理系统的设计与实现  

生成日期：2026-06-23  

更新日期：2026-06-26（路由、API 约定见《代码编写文档.md》；实现状态在本清单追踪）

说明：本清单按《原型图生成规范.md》§6 页面清单生成；**实现状态**随开发进度在本文档勾选更新。路由与 API 约定见《代码编写文档.md》§六、§七。

| 序号 | 端别 | 角色/门户 | 模块 | 页面标题 | 活动节点 | 实现状态 | 路由 | 主要 API | PNG | HTML | 备注 |
|---:|---|---|---|---|---|---|---|---|---|---|---|
"""
for r in rows_out:
    seq, platform, role, module_cn, page_title, activity, rel_png, rel_html, status, route, api, note = r
    out += f"| {seq} | {platform} | {role} | {module_cn} | {page_title} | {activity} | {status} | `{route}` | `{api}` | `{rel_png}` | `{rel_html}` | {note} |\n"

Path(ROOT / "页面清单.md").write_text(out, encoding="utf-8")
print(f"Updated {len(rows_out)} rows")
