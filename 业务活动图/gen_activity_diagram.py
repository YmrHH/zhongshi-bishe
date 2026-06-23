# -*- coding: utf-8 -*-
"""
业务活动图 PNG 生成
规范：《UML图规范.md》§十三、§十九；流程：《开题方案.md》§5.6 连线路由表
"""

from PIL import Image, ImageDraw, ImageFont
import glob
import os
import sys

DIR = os.path.dirname(__file__)
SCALE = 3
OUTPUT_DPI = 300
MIN_WIDTH = 2400
LANE_W = 280
OUTER = 56
CH_L8 = OUTER
CH_L10 = OUTER + 34
CH_R9 = OUTER + 34


def s(v):
    return v * SCALE


def load_font(size):
    scaled = max(11, int(size * SCALE))
    for path in (r"C:\Windows\Fonts\simhei.ttf", r"C:\Windows\Fonts\msyh.ttc"):
        if os.path.exists(path):
            return ImageFont.truetype(path, scaled)
    return ImageFont.load_default()


def ts(draw, text, font):
    b = draw.textbbox((0, 0), text, font=font)
    return b[2] - b[0], b[3] - b[1]


def box(draw, x, y, w, h, text, font, lw):
    draw.rounded_rectangle([x, y, x + w, y + h], radius=s(8), outline="black", width=lw, fill="white")
    tw, th = ts(draw, text, font)
    draw.text((x + (w - tw) // 2, y + (h - th) // 2), text, fill="black", font=font)


def diamond(draw, cx, cy, w, h, text, font, lw):
    hw, hh = w // 2, h // 2
    pts = [(cx, cy - hh), (cx + hw, cy), (cx, cy + hh), (cx - hw, cy)]
    draw.polygon(pts, outline="black", fill="white")
    for i in range(4):
        draw.line([pts[i], pts[(i + 1) % 4]], fill="black", width=lw)
    tw, th = ts(draw, text, font)
    draw.text((cx - tw // 2, cy - th // 2), text, fill="black", font=font)
    return hw, hh


def start_node(draw, cx, cy, r, lw):
    draw.ellipse([cx - r, cy - r, cx + r, cy + r], outline="black", width=lw, fill="black")


def end_node(draw, cx, cy, r, lw):
    draw.ellipse([cx - r, cy - r, cx + r, cy + r], outline="black", width=lw, fill="white")
    inner = max(2, int(r * 0.55))
    draw.ellipse([cx - inner, cy - inner, cx + inner, cy + inner], fill="black", outline="black", width=lw)


def vline(draw, x, y1, y2, lw):
    if y1 > y2:
        y1, y2 = y2, y1
    draw.line([(x, y1), (x, y2)], fill="black", width=lw)


def hline(draw, x1, x2, y, lw):
    if x1 > x2:
        x1, x2 = x2, x1
    draw.line([(x1, y), (x2, y)], fill="black", width=lw)


def arrow_down(draw, x, y1, y2, lw):
    vline(draw, x, y1, y2 - s(8), lw)
    draw.polygon([(x, y2), (x - s(5), y2 - s(11)), (x + s(5), y2 - s(11))], fill="black")


def arrow_right(draw, x1, x2, y, lw):
    hline(draw, x1, x2 - s(8), y, lw)
    draw.polygon([(x2, y), (x2 - s(11), y - s(5)), (x2 - s(11), y + s(5))], fill="black")


def arrow_left(draw, x1, x2, y, lw):
    hline(draw, x1, x2 + s(8), y, lw)
    draw.polygon([(x2, y), (x2 + s(11), y - s(5)), (x2 + s(11), y + s(5))], fill="black")


def label(draw, x, y, text, font):
    tw, th = ts(draw, text, font)
    draw.text((x - tw // 2, y - th // 2), text, fill="black", font=font)


class SwimlaneLayout:
    def __init__(self, n_lanes, left, top, lane_w):
        self.n = n_lanes
        self.left = left
        self.top = top
        self.lw = lane_w
        self.lane_cx = [left + (i + 0.5) * lane_w for i in range(n_lanes)]
        self.divider_x = [left + (i + 1) * lane_w for i in range(n_lanes - 1)]
        self.right = left + n_lanes * lane_w
        self.ch_l8 = left + s(CH_L8)
        self.ch_l10 = left + s(CH_L10)
        self.ch_r9 = self.right - s(CH_R9)

    def draw_dividers(self, draw, line_w, y0, y1):
        for dx in self.divider_x:
            vline(draw, dx, y0, y1, line_w)

    def draw_headers(self, draw, names, hh, font, line_w):
        for i, name in enumerate(names):
            x0 = self.left + i * self.lw
            x1 = x0 + self.lw
            draw.rectangle([x0, self.top, x1, self.top + hh], outline="black", width=line_w, fill="white")
            lines = name.split("\n")
            ths = [ts(draw, ln, font)[1] for ln in lines]
            ty = self.top + (hh - sum(ths)) // 2
            for ln in lines:
                tw, th = ts(draw, ln, font)
                draw.text((x0 + (self.lw - tw) // 2, ty), ln, fill="black", font=font)
                ty += th + s(3)


class DiagramRenderer:
    def __init__(self, lanes, title, caption, filename, nodes, edges_fn):
        self.lanes = lanes
        self.title = title
        self.caption = caption
        self.filename = filename
        self.nodes = nodes
        self.edges_fn = edges_fn
        self.line_w = s(2)
        self.ft = load_font(17)
        self.fl = load_font(12)
        self.fa = load_font(12)
        self.fd = load_font(10)
        self.fc = load_font(15)
        self.flb = load_font(10)
        self.rg = s(78)
        self.aw = s(210)
        self.ah = s(36)
        self.dh = s(62)
        self.dw = s(148)
        self.r = s(10)
        self.margin = s(36)
        self.th = s(44)
        self.hh = s(48)

    def ry(self, lt, row):
        return lt + self.hh + row * self.rg + self.rg // 2

    def render(self):
        max_row = max(n["row"] for n in self.nodes.values())
        ch = (max_row + 2) * self.rg
        lane_w = s(LANE_W)
        iw = max(MIN_WIDTH, self.margin * 2 + lane_w * len(self.lanes))
        ih = self.margin + self.th + self.hh + ch + s(70)

        img = Image.new("RGB", (iw, ih), "white")
        self.draw = ImageDraw.Draw(img)
        tw, _ = ts(self.draw, self.title, self.ft)
        self.draw.text(((iw - tw) // 2, self.margin), self.title, fill="black", font=self.ft)

        lt = self.margin + self.th
        ll = (iw - lane_w * len(self.lanes)) // 2
        self.lt = lt
        self.lay = SwimlaneLayout(len(self.lanes), ll, lt, lane_w)
        self.lay.draw_headers(self.draw, self.lanes, self.hh, self.fl, self.line_w)
        y_body_top = lt
        y_body_bot = lt + self.hh + ch
        self.lay.draw_dividers(self.draw, self.line_w, y_body_top, y_body_bot)

        self.anchors = {}
        for nid, n in self.nodes.items():
            self.anchors[nid] = self._draw_node(nid, n)

        self.edges_fn(self)
        self._draw_outer_border(y_body_top, y_body_bot)

        cw2, _ = ts(self.draw, self.caption, self.fc)
        self.draw.text(((iw - cw2) // 2, ih - s(44)), self.caption, fill="black", font=self.fc)
        out = os.path.join(DIR, self.filename)
        img.save(out, "PNG", dpi=(OUTPUT_DPI, OUTPUT_DPI))
        print(f"Saved: {out} ({iw}x{ih})")

    def _draw_node(self, nid, n):
        cx = self.lay.lane_cx[n["lane"]]
        cy = self.ry(self.lt, n["row"])
        t = n["type"]
        if t == "start":
            start_node(self.draw, cx, cy, self.r, self.line_w)
            return {"cx": cx, "cy": cy, "top": cy - self.r, "bot": cy + self.r,
                    "left": cx - self.r, "right": cx + self.r, "lane": n["lane"]}
        if t == "end":
            end_node(self.draw, cx, cy, self.r, self.line_w)
            return {"cx": cx, "cy": cy, "top": cy - self.r, "bot": cy + self.r,
                    "left": cx - self.r, "right": cx + self.r, "lane": n["lane"]}
        if t == "act":
            bx, by = cx - self.aw // 2, cy - self.ah // 2
            box(self.draw, bx, by, self.aw, self.ah, n["text"], self.fa, self.line_w)
            return {"cx": cx, "cy": cy, "top": by, "bot": by + self.ah,
                    "left": bx, "right": bx + self.aw, "lane": n["lane"]}
        hw, hh = diamond(self.draw, cx, cy, self.dw, self.dh, n["text"], self.fd, self.line_w)
        return {
            "cx": cx, "cy": cy, "hw": hw, "hh": hh,
            "top": cy - hh, "bot": cy + hh,
            "left": cx - hw, "right": cx + hw,
            "top_pt": (cx, cy - hh),
            "right_pt": (cx + hw, cy),
            "bottom_pt": (cx, cy + hh),
            "left_pt": (cx - hw, cy),
            "lane": n["lane"],
        }

    def _draw_outer_border(self, y_top, y_bot):
        x0, x1 = self.lay.left, self.lay.right
        lw = self.line_w
        vline(self.draw, x0, y_top, y_bot, lw)
        vline(self.draw, x1, y_top, y_bot, lw)
        hline(self.draw, x0, x1, y_bot, lw)

    def _seg_v(self, x, y1, y2):
        vline(self.draw, x, y1, y2, self.line_w)

    def _seg_h(self, x1, x2, y):
        hline(self.draw, x1, x2, y, self.line_w)

    def _term_down(self, x, y1, y2):
        arrow_down(self.draw, x, y1, y2, self.line_w)

    def _term_right(self, x1, x2, y):
        arrow_right(self.draw, x1, x2, y, self.line_w)

    def _term_left(self, x1, x2, y):
        arrow_left(self.draw, x1, x2, y, self.line_w)

    def _cross_h_direct(self, src, dst, y=None):
        """跨泳道同高水平直连（L2/L5/L11），单段水平箭头入目标侧。"""
        y = src["cy"] if y is None else y
        if dst["cx"] > src["cx"]:
            self._term_right(src["right"], dst["left"], y)
        else:
            self._term_left(src["left"], dst["right"], y)


def edges_diagram1(d):
    """《开题方案.md》§5.6 图3-1 连线路由 L1—L15（定稿 10 活动节点）。"""
    A = d.anchors
    ex = A["e"]["cx"]

    # L1：● → 需求信息确认
    d._term_down(A["s"]["cx"], A["s"]["bot"], A["a1"]["top"])

    # L2 / L9 汇点：需求信息确认 → 中试需求提交入线
    merge_a2 = A["a1"]["bot"] + (A["a2"]["top"] - A["a1"]["bot"]) // 3
    d._term_down(A["a1"]["cx"], A["a1"]["bot"], merge_a2)
    d._term_down(A["a2"]["cx"], merge_a2, A["a2"]["top"])

    # L3：中试需求提交 → 需求受理登记（同高水平直连）
    d._cross_h_direct(A["a2"], A["a3"])

    # L4：需求受理登记 → 判断1
    d._term_down(A["a3"]["cx"], A["a3"]["bot"], A["d1"]["top"])

    # L5：判断1 下顶角·否 → 需求退回通知
    bx, by = A["d1"]["bottom_pt"]
    d._term_down(bx, by, A["a4"]["top"])
    label(d.draw, bx - s(18), (by + A["a4"]["top"]) // 2, "否", d.flb)

    # L6：需求退回通知 → 需求材料补充（同高水平直连）
    d._cross_h_direct(A["a4"], A["a5"])

    # L7：判断1 右顶角·是 → 受理材料核验
    rx, ry = A["d1"]["right_pt"]
    d._seg_h(rx, A["a6"]["cx"], ry)
    d._term_down(A["a6"]["cx"], ry, A["a6"]["top"])
    label(d.draw, (rx + A["a6"]["cx"]) // 2, ry - s(16), "是", d.flb)

    # L8：受理材料核验 → 判断2
    d._term_down(A["a6"]["cx"], A["a6"]["bot"], A["d2"]["top"])

    # L9：需求材料补充 → 提交入线汇点（CH_L8 左侧独立通道）
    ch8 = d.lay.ch_l8
    y9 = A["a5"]["bot"] + s(14)
    d._seg_v(A["a5"]["cx"], A["a5"]["bot"], y9)
    d._seg_h(A["a5"]["cx"], ch8, y9)
    d._seg_v(ch8, y9, merge_a2)
    d._seg_h(ch8, A["a2"]["cx"], merge_a2)

    # L10：判断2 下顶角·是 → 受理结果通知
    bx2, by2 = A["d2"]["bottom_pt"]
    d._term_down(bx2, by2, A["a7"]["top"])
    label(d.draw, bx2 + s(18), (by2 + A["a7"]["top"]) // 2, "是", d.flb)

    # L11：判断2 左顶角·否 → ◉ 入线（最短路径，禁止绕左侧外框）
    lx, ly = A["d2"]["left_pt"]
    y_in = A["e"]["top"] - s(12)
    d._seg_v(lx, ly, y_in)
    d._seg_h(lx, ex, y_in)
    label(d.draw, lx - s(20), (ly + y_in) // 2, "否", d.flb)

    # L12：受理结果通知 → 受理回执签收（同高水平直连）
    d._cross_h_direct(A["a7"], A["a8"])

    # L13：受理回执签收 → 受理信息归档（同排水平直连）
    d._cross_h_direct(A["a8"], A["a9"])

    # L14：受理信息归档 → 查看受理进度（1 竖 + 1 横，最短）
    y14 = A["a10"]["cy"]
    d._seg_v(A["a9"]["cx"], A["a9"]["bot"], y14)
    d._term_left(A["a9"]["cx"], A["a10"]["right"], y14)

    # L15：查看受理进度 → ◉（直下 + 横至 ◉ 列，与 L11 共 y_in 后入 ◉）
    d._seg_v(A["a10"]["cx"], A["a10"]["bot"], y_in)
    d._seg_h(A["a10"]["cx"], ex, y_in)
    d._term_down(ex, y_in, A["e"]["top"])


def edges_diagram2(d):
    """《开题方案.md》§5.6 图3-2 连线路由 L1—L17（定稿 12 活动节点）。"""
    A = d.anchors
    ex = A["e"]["cx"]
    y_in = A["e"]["top"] - s(12)

    # L1 / L6 汇点：● → 评估前置核查
    merge_a1 = A["s"]["bot"] + (A["a1"]["top"] - A["s"]["bot"]) // 3
    d._term_down(A["s"]["cx"], A["s"]["bot"], merge_a1)
    d._term_down(A["a1"]["cx"], merge_a1, A["a1"]["top"])

    # L2：评估前置核查 → 中试条件评估
    d._term_down(A["a1"]["cx"], A["a1"]["bot"], A["a2"]["top"])

    # L3：中试条件评估 → 判断1
    d._term_down(A["a2"]["cx"], A["a2"]["bot"], A["d1"]["top"])

    # L4：判断1 下顶角·否 → 条件整改通知
    bx, by = A["d1"]["bottom_pt"]
    d._term_down(bx, by, A["a3"]["top"])
    label(d.draw, bx - s(18), (by + A["a3"]["top"]) // 2, "否", d.flb)

    # L5：条件整改通知 → 条件材料补充（同高水平直连）
    d._cross_h_direct(A["a3"], A["a4"])

    # L6：条件材料补充 → 前置核查入线（CH_L8 左侧独立通道）
    ch8 = d.lay.ch_l8
    y6 = A["a4"]["bot"] + s(14)
    d._seg_v(A["a4"]["cx"], A["a4"]["bot"], y6)
    d._seg_h(A["a4"]["cx"], ch8, y6)
    d._seg_v(ch8, y6, merge_a1)
    d._seg_h(ch8, A["a1"]["cx"], merge_a1)

    # L7：判断1 右顶角·是 → 资源需求核定
    rx, ry = A["d1"]["right_pt"]
    d._seg_h(rx, A["a5"]["cx"], ry)
    d._term_down(A["a5"]["cx"], ry, A["a5"]["top"])
    label(d.draw, (rx + A["a5"]["cx"]) // 2, ry - s(16), "是", d.flb)

    # L8：资源需求核定 → 技术可行性审（同高水平直连）
    d._cross_h_direct(A["a5"], A["a6"])

    # L9：技术可行性审 → 判断2；L12 汇入入线
    merge_a6 = A["a6"]["top"] - s(28)
    d._seg_v(A["a6"]["cx"], merge_a6, A["a6"]["top"])
    d._term_down(A["a6"]["cx"], A["a6"]["bot"], A["d2"]["top"])

    # L10：判断2 下顶角·否 → 评估材料补充（同高水平直连）
    bx2, by2 = A["d2"]["bottom_pt"]
    y10 = A["a7"]["cy"]
    d._seg_v(bx2, by2, y10)
    d._term_right(bx2, A["a7"]["left"], y10)
    label(d.draw, (bx2 + A["a7"]["cx"]) // 2, y10 + s(16), "否", d.flb)

    # L11：判断2 右顶角·是 → 评估结论出具
    rx2, ry2 = A["d2"]["right_pt"]
    d._seg_h(rx2, A["a8"]["cx"], ry2)
    d._term_down(A["a8"]["cx"], ry2, A["a8"]["top"])
    label(d.draw, (rx2 + A["a8"]["cx"]) // 2, ry2 - s(16), "是", d.flb)

    # L12：评估材料补充 → 可行性审入线（右侧最短通道 ↺）
    chr9 = d.lay.ch_r9
    y12 = A["a7"]["bot"] + s(14)
    d._seg_v(A["a7"]["cx"], A["a7"]["bot"], y12)
    d._seg_h(A["a7"]["cx"], chr9, y12)
    d._seg_v(chr9, y12, merge_a6)
    d._seg_h(chr9, A["a6"]["cx"], merge_a6)

    # L13：评估结论出具 → 评估结果签收（同高水平直连）
    d._cross_h_direct(A["a8"], A["a9"])

    # L14：评估结果签收 → 评估意见反馈（同泳道竖直直连）
    d._term_down(A["a9"]["cx"], A["a9"]["bot"], A["a10"]["top"])

    # L15：评估意见反馈 → 评估信息归档（同高水平直连）
    d._cross_h_direct(A["a10"], A["a11"])

    # L16：评估信息归档 → 查看评估结论（同高水平直连）
    d._cross_h_direct(A["a11"], A["a12"])

    # L17：查看评估结论 → ◉（短距汇入 ◉ 上端）
    d._seg_v(A["a12"]["cx"], A["a12"]["bot"], y_in)
    d._seg_h(A["a12"]["cx"], ex, y_in)
    d._term_down(ex, y_in, A["e"]["top"])


def check_diagram2(d):
    """§十九 生图后逐点检查（图3-2，定稿 12 节点）。"""
    A = d.anchors
    checks = [
        ("A1 起止节点", A.get("s") and A.get("e")),
        ("B1 活动=12", sum(1 for v in d.nodes.values() if v["type"] == "act") == 12),
        ("B2 判断=2", sum(1 for v in d.nodes.values() if v["type"] == "dec") == 2),
        ("C2 L8/L13/L16 同排", d.nodes["a5"]["row"] == d.nodes["a6"]["row"]
         and d.nodes["a8"]["row"] == d.nodes["a9"]["row"]
         and d.nodes["a11"]["row"] == d.nodes["a12"]["row"]),
        ("C4 L6/L12 路径分离", d.lay.ch_l8 != d.lay.ch_r9),
        ("D1 节点齐全", all(k in A for k in (
            "s", "a1", "a2", "d1", "a3", "a4", "a5", "a6", "d2", "a7", "a8",
            "a9", "a10", "a11", "a12", "e"))),
    ]
    print("--- §十九 图3-2 自检 ---")
    ok = True
    for name, passed in checks:
        status = "PASS" if passed else "FAIL"
        print(f"  [{status}] {name}")
        ok = ok and passed
    print(f"  结论: {'通过' if ok else '不通过，须修正'}")
    return ok


def check_diagram1(d):
    """§十九 生图后逐点检查（图3-1，定稿 10 节点）。"""
    A = d.anchors
    checks = [
        ("A1 起止节点", A.get("s") and A.get("e")),
        ("B1 活动=10", sum(1 for k, v in d.nodes.items() if v["type"] == "act") == 10),
        ("B2 判断=2", sum(1 for k, v in d.nodes.items() if v["type"] == "dec") == 2),
        ("C2 L3/L6/L12/L13 同排", d.nodes["a2"]["row"] == d.nodes["a3"]["row"]
         and d.nodes["a4"]["row"] == d.nodes["a5"]["row"]
         and d.nodes["a7"]["row"] == d.nodes["a8"]["row"]
         and d.nodes["a8"]["row"] == d.nodes["a9"]["row"]),
        ("C4 L9/L11 路径分离", d.lay.ch_l8 != d.lay.ch_l10),
        ("D1 节点齐全", all(k in A for k in (
            "s", "a1", "a2", "a3", "d1", "a4", "a5", "a6", "d2",
            "a7", "a8", "a9", "a10", "e"))),
    ]
    print("--- §十九 图3-1 自检 ---")
    ok = True
    for name, passed in checks:
        status = "PASS" if passed else "FAIL"
        print(f"  [{status}] {name}")
        ok = ok and passed
    print(f"  结论: {'通过' if ok else '不通过，须修正'}")
    return ok


def edges_diagram3(d):
    """《开题方案.md》§5.6 图3-3 连线路由 L1—L16（定稿 11 活动节点）。"""
    A = d.anchors
    ex = A["e"]["cx"]
    y_in = A["e"]["top"] - s(12)

    # L1 / L3 汇点：● → 中试资源匹配
    merge_a1 = A["s"]["bot"] + (A["a1"]["top"] - A["s"]["bot"]) // 3
    d._term_down(A["s"]["cx"], A["s"]["bot"], merge_a1)
    d._term_down(A["a1"]["cx"], merge_a1, A["a1"]["top"])

    # L2：中试资源匹配 → 判断1
    d._term_down(A["a1"]["cx"], A["a1"]["bot"], A["d1"]["top"])

    # L3：判断1 下顶角·否 → 匹配入线（左侧最短通道 ↺）
    ch8 = d.lay.ch_l8
    bx, by = A["d1"]["bottom_pt"]
    y3 = by + s(14)
    d._seg_v(bx, by, y3)
    d._seg_h(bx, ch8, y3)
    d._seg_v(ch8, y3, merge_a1)
    d._seg_h(ch8, A["a1"]["cx"], merge_a1)
    label(d.draw, bx - s(18), y3 + s(14), "否", d.flb)

    # L4：判断1 右顶角·是 → 中试任务派发
    rx, ry = A["d1"]["right_pt"]
    d._seg_h(rx, A["a2"]["cx"], ry)
    d._term_down(A["a2"]["cx"], ry, A["a2"]["top"])
    label(d.draw, (rx + A["a2"]["cx"]) // 2, ry - s(16), "是", d.flb)

    # L5：中试任务派发 → 派单结果通知
    d._term_down(A["a2"]["cx"], A["a2"]["bot"], A["a3"]["top"])

    # L6：派单结果通知 → 接收任务执行（同高水平直连）
    d._cross_h_direct(A["a3"], A["a4"])

    # L7：接收任务执行 → 任务接收确认；L12 汇入入线
    merge_a4 = A["a4"]["top"] - s(28)
    d._seg_v(A["a4"]["cx"], merge_a4, A["a4"]["top"])
    d._term_down(A["a4"]["cx"], A["a4"]["bot"], A["a5"]["top"])

    # L8：任务接收确认 → 填报执行进度
    d._term_down(A["a5"]["cx"], A["a5"]["bot"], A["a6"]["top"])

    # L9：填报执行进度 → 执行进度通报（同高水平直连）
    d._cross_h_direct(A["a6"], A["a7"])

    # L10：执行进度通报 → 判断2
    d._term_down(A["a7"]["cx"], A["a7"]["bot"], A["d2"]["top"])

    # L11：判断2 下顶角·是 → 异常任务重派（调度员泳道内竖直直连）
    bx2, by2 = A["d2"]["bottom_pt"]
    d._term_down(bx2, by2, A["a8"]["top"])
    label(d.draw, bx2 + s(18), (by2 + A["a8"]["top"]) // 2, "是", d.flb)

    # L12：异常任务重派 → 接收执行入线（右侧最短通道 ↺ 至技术人员列）
    chr9 = d.lay.ch_r9
    y12 = A["a8"]["bot"] + s(14)
    d._seg_v(A["a8"]["cx"], A["a8"]["bot"], y12)
    d._seg_h(A["a8"]["cx"], chr9, y12)
    d._seg_v(chr9, y12, merge_a4)
    d._seg_h(chr9, A["a4"]["cx"], merge_a4)

    # L13：判断2 左顶角·否 → 执行结果确认（调度员泳道）
    lx, ly = A["d2"]["left_pt"]
    y13 = A["a9"]["top"] - s(8)
    d._seg_v(lx, ly, y13)
    d._seg_h(lx, A["a9"]["cx"], y13)
    d._term_down(A["a9"]["cx"], y13, A["a9"]["top"])
    label(d.draw, lx - s(20), (ly + y13) // 2, "否", d.flb)

    # L14：执行结果确认 → 查看执行进度（1 竖 + 1 横至同排）
    y14 = A["a10"]["cy"]
    d._seg_v(A["a9"]["cx"], A["a9"]["bot"], y14)
    d._term_right(A["a9"]["cx"], A["a10"]["left"], y14)

    # L15：查看执行进度 → 调度信息归档（同高水平直连）
    d._cross_h_direct(A["a10"], A["a11"])

    # L16：调度信息归档 → ◉（短距直入）
    d._seg_v(A["a11"]["cx"], A["a11"]["bot"], y_in)
    d._seg_h(A["a11"]["cx"], ex, y_in)
    d._term_down(ex, y_in, A["e"]["top"])


def check_diagram3(d):
    """§十九 生图后逐点检查（图3-3，定稿 11 节点）。"""
    A = d.anchors
    checks = [
        ("A1 起止节点", A.get("s") and A.get("e")),
        ("B1 活动=11", sum(1 for v in d.nodes.values() if v["type"] == "act") == 11),
        ("B2 判断=2", sum(1 for v in d.nodes.values() if v["type"] == "dec") == 2),
        ("C2 L6/L15 同排", d.nodes["a3"]["row"] == d.nodes["a4"]["row"]
         and d.nodes["a10"]["row"] == d.nodes["a11"]["row"]),
        ("C4 L3/L12 路径分离", d.lay.ch_l8 != d.lay.ch_r9),
        ("D1 节点齐全", all(k in A for k in (
            "s", "a1", "d1", "a2", "a3", "a4", "a5", "a6", "a7", "d2",
            "a8", "a9", "a10", "a11", "e"))),
    ]
    print("--- §十九 图3-3 自检 ---")
    ok = True
    for name, passed in checks:
        status = "PASS" if passed else "FAIL"
        print(f"  [{status}] {name}")
        ok = ok and passed
    print(f"  结论: {'通过' if ok else '不通过，须修正'}")
    return ok


def edges_diagram4(d):
    """《开题方案.md》§5.6 图3-4 连线路由 L1—L18（定稿 11 活动节点）。"""
    A = d.anchors
    ex = A["e"]["cx"]
    y_in = A["e"]["top"] - s(12)

    # L1 / L12 汇点：● → 试验结果提交
    merge_a1 = A["s"]["bot"] + (A["a1"]["top"] - A["s"]["bot"]) // 3
    d._term_down(A["s"]["cx"], A["s"]["bot"], merge_a1)
    d._term_down(A["a1"]["cx"], merge_a1, A["a1"]["top"])

    # L2：试验结果提交 → 试验数据校验
    d._term_down(A["a1"]["cx"], A["a1"]["bot"], A["a2"]["top"])

    # L3：试验数据校验 → 中试报告审核（同高水平直连）
    d._cross_h_direct(A["a2"], A["a3"])

    # L4：中试报告审核 → 判断1
    d._term_down(A["a3"]["cx"], A["a3"]["bot"], A["d1"]["top"])

    merge_a4 = A["a4"]["top"] - s(28)
    d._seg_v(A["a4"]["cx"], merge_a4, A["a4"]["top"])

    # L5：判断1 下顶角·否 → 试验结果修改
    bx, by = A["d1"]["bottom_pt"]
    y5 = A["a4"]["cy"]
    d._seg_v(bx, by, y5)
    d._term_left(bx, A["a4"]["right"], y5)
    label(d.draw, bx - s(18), y5 + s(16), "否", d.flb)

    # L6：判断1 右顶角·是 → 结果复核确认
    rx, ry = A["d1"]["right_pt"]
    d._seg_h(rx, A["a5"]["cx"], ry)
    d._term_down(A["a5"]["cx"], ry, A["a5"]["top"])
    label(d.draw, (rx + A["a5"]["cx"]) // 2, ry - s(16), "是", d.flb)

    # L7：结果复核确认 → 判断2
    d._term_down(A["a5"]["cx"], A["a5"]["bot"], A["d2"]["top"])

    # L8：判断2 下顶角·否 → 修改入线汇点（CH_L10，与 L5 错 Y）
    ch10 = d.lay.ch_l10
    bx2, by2 = A["d2"]["bottom_pt"]
    y8 = by2 + s(14)
    d._seg_v(bx2, by2, y8)
    d._seg_h(bx2, ch10, y8)
    d._seg_v(ch10, y8, merge_a4)
    d._seg_h(ch10, A["a4"]["cx"], merge_a4)
    label(d.draw, bx2 - s(18), y8 + s(12), "否", d.flb)

    # L9：判断2 右顶角·是 → 中试报告归档
    rx2, ry2 = A["d2"]["right_pt"]
    d._seg_h(rx2, A["a6"]["cx"], ry2)
    d._term_down(A["a6"]["cx"], ry2, A["a6"]["top"])
    label(d.draw, (rx2 + A["a6"]["cx"]) // 2, ry2 - s(16), "是", d.flb)

    # L10：中试报告归档 → 复核结果通知
    d._term_down(A["a6"]["cx"], A["a6"]["bot"], A["a7"]["top"])

    # L11：复核结果通知 → 复核意见反馈（同高水平直连）
    d._cross_h_direct(A["a7"], A["a8"])

    merge_a8 = A["a8"]["top"] - s(28)
    d._seg_v(A["a8"]["cx"], merge_a8, A["a8"]["top"])

    # L12：试验结果修改 → 提交入线（左侧最短通道 ↺）
    ch8 = d.lay.ch_l8
    y12 = A["a4"]["bot"] + s(14)
    d._seg_v(A["a4"]["cx"], A["a4"]["bot"], y12)
    d._seg_h(A["a4"]["cx"], ch8, y12)
    d._seg_v(ch8, y12, merge_a1)
    d._seg_h(ch8, A["a1"]["cx"], merge_a1)

    # L13：复核意见反馈 → 反馈结果审核（同高水平直连）
    d._cross_h_direct(A["a8"], A["a9"])

    # 反馈结果审核 → 判断3
    d._term_down(A["a9"]["cx"], A["a9"]["bot"], A["d3"]["top"])

    # L14：判断3 下顶角·否 → 反馈入线（左侧最短，与 L12 错层）
    bx3, by3 = A["d3"]["bottom_pt"]
    y14 = by3 + s(14)
    d._seg_v(bx3, by3, y14)
    d._seg_h(bx3, ch8, y14)
    d._seg_v(ch8, y14, merge_a8)
    d._seg_h(ch8, A["a8"]["cx"], merge_a8)
    label(d.draw, bx3 - s(18), y14 + s(12), "否", d.flb)

    # L16：判断3 右顶角·是 → 查看复核结果
    rx3, ry3 = A["d3"]["right_pt"]
    y16 = A["a10"]["cy"]
    d._seg_v(rx3, ry3, y16)
    d._term_right(rx3, A["a10"]["left"], y16)
    label(d.draw, (rx3 + A["a10"]["cx"]) // 2, ry3 - s(16), "是", d.flb)

    # L17：查看复核结果 → 报告信息归档（同高水平直连）
    d._cross_h_direct(A["a10"], A["a11"])

    # L18：报告信息归档 → ◉（短距直入）
    d._seg_v(A["a11"]["cx"], A["a11"]["bot"], y_in)
    d._seg_h(A["a11"]["cx"], ex, y_in)
    d._term_down(ex, y_in, A["e"]["top"])


def edges_diagram5(d):
    """《开题方案.md》§5.6 图3-5 连线路由 L1—L14（定稿 9 活动节点）。"""
    A = d.anchors
    ex = A["e"]["cx"]
    y_in = A["e"]["top"] - s(12)

    # L1 / L3 汇点：● → 项目台账维护
    merge_a1 = A["s"]["bot"] + (A["a1"]["top"] - A["s"]["bot"]) // 3
    d._term_down(A["s"]["cx"], A["s"]["bot"], merge_a1)
    d._term_down(A["a1"]["cx"], merge_a1, A["a1"]["top"])

    # L2：项目台账维护 → 判断1
    d._term_down(A["a1"]["cx"], A["a1"]["bot"], A["d1"]["top"])

    # L3：判断1 下顶角·否 → 台账入线（左侧最短 ↺）
    ch8 = d.lay.ch_l8
    bx, by = A["d1"]["bottom_pt"]
    y3 = by + s(14)
    d._seg_v(bx, by, y3)
    d._seg_h(bx, ch8, y3)
    d._seg_v(ch8, y3, merge_a1)
    d._seg_h(ch8, A["a1"]["cx"], merge_a1)
    label(d.draw, bx - s(18), y3 + s(14), "否", d.flb)

    # L4：判断1 右顶角·是 → 档案信息确认
    rx, ry = A["d1"]["right_pt"]
    d._seg_h(rx, A["a2"]["cx"], ry)
    d._term_down(A["a2"]["cx"], ry, A["a2"]["top"])
    label(d.draw, (rx + A["a2"]["cx"]) // 2, ry - s(16), "是", d.flb)

    # L5：档案信息确认 → 结案资料归集
    d._term_down(A["a2"]["cx"], A["a2"]["bot"], A["a3"]["top"])

    # L6：结案资料归集 → 中试周期统计（同高水平直连）
    d._cross_h_direct(A["a3"], A["a4"])

    # L7：中试周期统计 → 成功率分析；L9 汇入入线
    merge_a4 = A["a4"]["top"] - s(28)
    d._seg_v(A["a4"]["cx"], merge_a4, A["a4"]["top"])
    d._term_down(A["a4"]["cx"], A["a4"]["bot"], A["a5"]["top"])

    # L8：成功率分析 → 判断2
    d._term_down(A["a5"]["cx"], A["a5"]["bot"], A["d2"]["top"])

    # L9：判断2 下顶角·否 → 周期统计入线（右侧最短 ↺）
    chr9 = d.lay.ch_r9
    bx2, by2 = A["d2"]["bottom_pt"]
    y9 = by2 + s(14)
    d._seg_v(bx2, by2, y9)
    d._seg_h(bx2, chr9, y9)
    d._seg_v(chr9, y9, merge_a4)
    d._seg_h(chr9, A["a4"]["cx"], merge_a4)
    label(d.draw, bx2 + s(18), y9 + s(12), "否", d.flb)

    # L10：判断2 右顶角·是 → 服务简报生成
    rx2, ry2 = A["d2"]["right_pt"]
    d._seg_h(rx2, A["a6"]["cx"], ry2)
    d._term_down(A["a6"]["cx"], ry2, A["a6"]["top"])
    label(d.draw, (rx2 + A["a6"]["cx"]) // 2, ry2 - s(16), "是", d.flb)

    # L11：服务简报生成 → 简报内容审核（调度员 → 审核员，跨泳道）
    y11 = (A["a6"]["bot"] + A["a7"]["top"]) // 2
    d._seg_v(A["a6"]["cx"], A["a6"]["bot"], y11)
    d._seg_h(A["a6"]["cx"], A["a7"]["cx"], y11)
    d._term_down(A["a7"]["cx"], y11, A["a7"]["top"])

    # L12：简报内容审核 → 查看服务简报（同高水平直连）
    d._cross_h_direct(A["a7"], A["a8"])

    # L13：查看服务简报 → 档案信息归档（企业 → 审核员）
    y13 = (A["a8"]["bot"] + A["a9"]["top"]) // 2
    d._seg_v(A["a8"]["cx"], A["a8"]["bot"], y13)
    d._seg_h(A["a8"]["cx"], A["a9"]["cx"], y13)
    d._term_down(A["a9"]["cx"], y13, A["a9"]["top"])

    # L14：档案信息归档 → ◉（短距直入）
    d._seg_v(A["a9"]["cx"], A["a9"]["bot"], y_in)
    d._seg_h(A["a9"]["cx"], ex, y_in)
    d._term_down(ex, y_in, A["e"]["top"])


def check_diagram4(d):
    """§十九 生图后逐点检查（图3-4，定稿 11 节点）。"""
    A = d.anchors
    checks = [
        ("A1 起止节点", A.get("s") and A.get("e")),
        ("B1 活动=11", sum(1 for v in d.nodes.values() if v["type"] == "act") == 11),
        ("B2 判断=3", sum(1 for v in d.nodes.values() if v["type"] == "dec") == 3),
        ("C2 L3/L17 同排", d.nodes["a2"]["row"] == d.nodes["a3"]["row"]
         and d.nodes["a10"]["row"] == d.nodes["a11"]["row"]),
        ("C4 L8/L12/L14 通道分离", d.lay.ch_l8 != d.lay.ch_l10),
        ("D1 节点齐全", all(k in A for k in (
            "s", "a1", "a2", "a3", "d1", "a4", "a5", "d2", "a6", "a7", "a8",
            "a9", "d3", "a10", "a11", "e"))),
    ]
    print("--- §十九 图3-4 自检 ---")
    ok = True
    for name, passed in checks:
        status = "PASS" if passed else "FAIL"
        print(f"  [{status}] {name}")
        ok = ok and passed
    print(f"  结论: {'通过' if ok else '不通过，须修正'}")
    return ok


def check_diagram5(d):
    """§十九 生图后逐点检查（图3-5，定稿 9 节点）。"""
    A = d.anchors
    checks = [
        ("A1 起止节点", A.get("s") and A.get("e")),
        ("B1 活动=9", sum(1 for v in d.nodes.values() if v["type"] == "act") == 9),
        ("B2 判断=2", sum(1 for v in d.nodes.values() if v["type"] == "dec") == 2),
        ("C2 L6/L12 同排", d.nodes["a3"]["row"] == d.nodes["a4"]["row"]
         and d.nodes["a7"]["row"] == d.nodes["a8"]["row"]),
        ("C4 L3/L9 路径分离", d.lay.ch_l8 != d.lay.ch_r9),
        ("D1 节点齐全", all(k in A for k in (
            "s", "a1", "d1", "a2", "a3", "a4", "a5", "d2", "a6", "a7", "a8", "a9", "e"))),
    ]
    print("--- §十九 图3-5 自检 ---")
    ok = True
    for name, passed in checks:
        status = "PASS" if passed else "FAIL"
        print(f"  [{status}] {name}")
        ok = ok and passed
    print(f"  结论: {'通过' if ok else '不通过，须修正'}")
    return ok



def diagram1():
    nodes = {
        "s": {"type": "start", "lane": 0, "row": 0},
        "a1": {"type": "act", "lane": 0, "row": 1, "text": "需求信息确认"},
        "a2": {"type": "act", "lane": 0, "row": 2, "text": "中试需求提交"},
        "a3": {"type": "act", "lane": 1, "row": 2, "text": "需求受理登记"},
        "d1": {"type": "dec", "lane": 1, "row": 3, "text": "材料是否\n齐全合规"},
        "a5": {"type": "act", "lane": 0, "row": 4, "text": "需求材料补充"},
        "a4": {"type": "act", "lane": 1, "row": 4, "text": "需求退回通知"},
        "a6": {"type": "act", "lane": 2, "row": 4, "text": "受理材料核验"},
        "d2": {"type": "dec", "lane": 2, "row": 5, "text": "是否同意\n受理"},
        "a7": {"type": "act", "lane": 2, "row": 6, "text": "受理结果通知"},
        "a8": {"type": "act", "lane": 0, "row": 6, "text": "受理回执签收"},
        "a9": {"type": "act", "lane": 1, "row": 6, "text": "受理信息归档"},
        "a10": {"type": "act", "lane": 0, "row": 7, "text": "查看受理进度"},
        "e": {"type": "end", "lane": 1, "row": 8},
    }

    class R(DiagramRenderer):
        def render(self):
            super().render()
            for nid, n in self.nodes.items():
                if nid in self.anchors:
                    self.anchors[nid]["row"] = n["row"]
            check_diagram1(self)

    R(
        ["企业项目负责人\n（Web/小程序）", "中试调度员\n（Web）", "中试审核员\n（Web）"],
        "核心活动1：中试需求管理",
        "图 3-1 中试需求管理业务活动图",
        "业务活动图1-中试需求管理.png",
        nodes,
        edges_diagram1,
    ).render()


def diagram2():
    nodes = {
        "s": {"type": "start", "lane": 0, "row": 0},
        "a1": {"type": "act", "lane": 0, "row": 1, "text": "评估前置核查"},
        "a2": {"type": "act", "lane": 0, "row": 2, "text": "中试条件评估"},
        "d1": {"type": "dec", "lane": 0, "row": 3, "text": "是否具备\n中试条件"},
        "a3": {"type": "act", "lane": 0, "row": 4, "text": "条件整改通知"},
        "a4": {"type": "act", "lane": 2, "row": 4, "text": "条件材料补充"},
        "a5": {"type": "act", "lane": 0, "row": 5, "text": "资源需求核定"},
        "a6": {"type": "act", "lane": 1, "row": 5, "text": "技术可行性审"},
        "d2": {"type": "dec", "lane": 1, "row": 6, "text": "技术是否\n具备可行性"},
        "a7": {"type": "act", "lane": 2, "row": 7, "text": "评估材料补充"},
        "a8": {"type": "act", "lane": 1, "row": 7, "text": "评估结论出具"},
        "a9": {"type": "act", "lane": 2, "row": 7, "text": "评估结果签收"},
        "a10": {"type": "act", "lane": 2, "row": 8, "text": "评估意见反馈"},
        "a11": {"type": "act", "lane": 0, "row": 9, "text": "评估信息归档"},
        "a12": {"type": "act", "lane": 2, "row": 9, "text": "查看评估结论"},
        "e": {"type": "end", "lane": 1, "row": 10},
    }

    class R(DiagramRenderer):
        def render(self):
            super().render()
            check_diagram2(self)

    R(
        ["中试调度员\n（Web）", "中试审核员\n（Web）", "企业项目负责人\n（Web/小程序）"],
        "核心活动2：中试评估管理",
        "图 3-2 中试评估管理业务活动图",
        "业务活动图2-中试评估管理.png",
        nodes,
        edges_diagram2,
    ).render()


def diagram3():
    nodes = {
        "s": {"type": "start", "lane": 0, "row": 0},
        "a1": {"type": "act", "lane": 0, "row": 1, "text": "中试资源匹配"},
        "d1": {"type": "dec", "lane": 0, "row": 2, "text": "资源是否\n匹配成功"},
        "a2": {"type": "act", "lane": 0, "row": 3, "text": "中试任务派发"},
        "a3": {"type": "act", "lane": 0, "row": 4, "text": "派单结果通知"},
        "a4": {"type": "act", "lane": 1, "row": 4, "text": "接收任务执行"},
        "a5": {"type": "act", "lane": 1, "row": 5, "text": "任务接收确认"},
        "a6": {"type": "act", "lane": 1, "row": 6, "text": "填报执行进度"},
        "a7": {"type": "act", "lane": 0, "row": 7, "text": "执行进度通报"},
        "d2": {"type": "dec", "lane": 0, "row": 8, "text": "是否异常\n需重派"},
        "a8": {"type": "act", "lane": 0, "row": 9, "text": "异常任务重派"},
        "a9": {"type": "act", "lane": 0, "row": 10, "text": "执行结果确认"},
        "a10": {"type": "act", "lane": 2, "row": 11, "text": "查看执行进度"},
        "a11": {"type": "act", "lane": 0, "row": 11, "text": "调度信息归档"},
        "e": {"type": "end", "lane": 1, "row": 12},
    }

    class R(DiagramRenderer):
        def render(self):
            super().render()
            check_diagram3(self)

    R(
        ["中试调度员\n（Web）", "中试技术人员\n（Web/小程序）", "企业项目负责人\n（Web/小程序）"],
        "核心活动3：中试调度管理",
        "图 3-3 中试调度管理业务活动图",
        "业务活动图3-中试调度管理.png",
        nodes,
        edges_diagram3,
    ).render()


def diagram4():
    nodes = {
        "s": {"type": "start", "lane": 0, "row": 0},
        "a1": {"type": "act", "lane": 0, "row": 1, "text": "试验结果提交"},
        "a2": {"type": "act", "lane": 0, "row": 2, "text": "试验数据校验"},
        "a3": {"type": "act", "lane": 1, "row": 2, "text": "中试报告审核"},
        "d1": {"type": "dec", "lane": 1, "row": 3, "text": "报告是否\n审核合格"},
        "a4": {"type": "act", "lane": 0, "row": 4, "text": "试验结果修改"},
        "a5": {"type": "act", "lane": 1, "row": 4, "text": "结果复核确认"},
        "d2": {"type": "dec", "lane": 1, "row": 5, "text": "复核是否\n确认通过"},
        "a6": {"type": "act", "lane": 1, "row": 6, "text": "中试报告归档"},
        "a7": {"type": "act", "lane": 1, "row": 7, "text": "复核结果通知"},
        "a8": {"type": "act", "lane": 2, "row": 8, "text": "复核意见反馈"},
        "a9": {"type": "act", "lane": 1, "row": 9, "text": "反馈结果审核"},
        "d3": {"type": "dec", "lane": 1, "row": 10, "text": "反馈是否\n审核通过"},
        "a10": {"type": "act", "lane": 2, "row": 11, "text": "查看复核结果"},
        "a11": {"type": "act", "lane": 1, "row": 11, "text": "报告信息归档"},
        "e": {"type": "end", "lane": 1, "row": 12},
    }

    class R(DiagramRenderer):
        def render(self):
            super().render()
            check_diagram4(self)

    R(
        ["中试技术人员\n（Web/小程序）", "中试审核员\n（Web）", "企业项目负责人\n（Web/小程序）"],
        "核心活动4：中试反馈管理",
        "图 3-4 中试反馈管理业务活动图",
        "业务活动图4-中试反馈管理.png",
        nodes,
        edges_diagram4,
    ).render()


def diagram5():
    nodes = {
        "s": {"type": "start", "lane": 0, "row": 0},
        "a1": {"type": "act", "lane": 0, "row": 1, "text": "项目台账维护"},
        "d1": {"type": "dec", "lane": 0, "row": 2, "text": "台账信息\n是否完整"},
        "a2": {"type": "act", "lane": 0, "row": 3, "text": "档案信息确认"},
        "a3": {"type": "act", "lane": 0, "row": 4, "text": "结案资料归集"},
        "a4": {"type": "act", "lane": 1, "row": 4, "text": "中试周期统计"},
        "a5": {"type": "act", "lane": 1, "row": 5, "text": "成功率分析"},
        "d2": {"type": "dec", "lane": 1, "row": 6, "text": "统计数据\n是否可用"},
        "a6": {"type": "act", "lane": 1, "row": 7, "text": "服务简报生成"},
        "a7": {"type": "act", "lane": 0, "row": 8, "text": "简报内容审核"},
        "a8": {"type": "act", "lane": 2, "row": 8, "text": "查看服务简报"},
        "a9": {"type": "act", "lane": 0, "row": 9, "text": "档案信息归档"},
        "e": {"type": "end", "lane": 1, "row": 10},
    }

    class R(DiagramRenderer):
        def render(self):
            super().render()
            check_diagram5(self)

    R(
        ["中试审核员\n（Web）", "中试调度员\n（Web）", "企业项目负责人\n（Web/小程序）"],
        "核心活动5：中试档案管理",
        "图 3-5 中试档案管理业务活动图",
        "业务活动图5-中试档案管理.png",
        nodes,
        edges_diagram5,
    ).render()


def delete_old(pattern):
    for p in glob.glob(os.path.join(DIR, pattern)):
        os.remove(p)
        print(f"Deleted: {p}")


if __name__ == "__main__":
    n = sys.argv[1] if len(sys.argv) > 1 else "1"
    if n == "1":
        delete_old("业务活动图1-*.png")
        diagram1()
    elif n == "2":
        delete_old("业务活动图2-*.png")
        diagram2()
    elif n == "3":
        delete_old("业务活动图3-*.png")
        diagram3()
    elif n == "4":
        delete_old("业务活动图4-*.png")
        diagram4()
    elif n == "5":
        delete_old("业务活动图5-*.png")
        diagram5()
    else:
        print(f"Diagram {n} not implemented yet.")
