# -*- coding: utf-8 -*-
"""Generate high-resolution functional module diagram PNG per 生图规范.txt"""

from PIL import Image, ImageDraw, ImageFont
import argparse
import os

DIR = os.path.dirname(__file__)
SCALE = 3
OUTPUT_DPI = 300
MIN_WIDTH = 2400
MIN_HEIGHT = 1200

TOPICS = {
    2: {
        "title": "广州生产力促进中心中试服务管理系统的设计与实现",
        "modules": [
            {
                "name": "中试需求管理",
                "children": ["中试需求提交", "需求材料补充", "需求受理登记", "需求退回通知"],
            },
            {
                "name": "中试评估管理",
                "children": ["中试条件评估", "技术可行性审", "资源需求核定", "评估结论出具"],
            },
            {
                "name": "中试调度管理",
                "children": ["中试资源匹配", "中试任务派发", "执行进度跟踪", "异常任务重派"],
            },
            {
                "name": "中试反馈管理",
                "children": ["试验结果提交", "中试报告审核", "结果复核确认", "中试报告归档"],
            },
            {
                "name": "中试档案管理",
                "children": ["项目台账维护", "中试周期统计", "成功率分析", "服务简报生成"],
            },
        ],
    },
    3: {
        "title": "广州大学城双创中心仪器共享管理平台的设计与实现",
        "modules": [
            {
                "name": "仪器编目管理",
                "children": ["仪器信息登记", "仪器状态更新", "仪器目录发布", "仪器下架处理"],
            },
            {
                "name": "预约申请管理",
                "children": ["仪器预约申请", "预约资格审核", "预约确认通知", "预约取消处理"],
            },
            {
                "name": "机时调度管理",
                "children": ["机时冲突检测", "机时调整通知", "排期方案生成", "调度结果确认"],
            },
            {
                "name": "使用确认管理",
                "children": ["使用过程登记", "使用结果确认", "异常申诉提交", "异常申诉复核"],
            },
            {
                "name": "共享效能管理",
                "children": ["机时使用统计", "共享效能分析", "高校贡献排名", "月度简报生成"],
            },
        ],
    },
}


def s(value: int) -> int:
    return value * SCALE


def load_font(size: int):
    scaled = max(12, int(size * SCALE))
    for path in (
        r"C:\Windows\Fonts\simhei.ttf",
        r"C:\Windows\Fonts\msyh.ttc",
        r"C:\Windows\Fonts\simsun.ttc",
    ):
        if os.path.exists(path):
            return ImageFont.truetype(path, scaled)
    return ImageFont.load_default()


def text_size(draw, text, font):
    bbox = draw.textbbox((0, 0), text, font=font)
    return bbox[2] - bbox[0], bbox[3] - bbox[1]


def draw_box(draw, x, y, w, h, text, font, line_w, vertical=False):
    draw.rectangle([x, y, x + w, y + h], outline="black", width=line_w, fill="white")
    if vertical:
        cx = x + w // 2
        cy = y + h // 2
        total_h = sum(text_size(draw, ch, font)[1] for ch in text)
        start_y = cy - total_h // 2
        for ch in text:
            cw, ch_h = text_size(draw, ch, font)
            draw.text((cx - cw // 2, start_y), ch, fill="black", font=font)
            start_y += ch_h
    else:
        tw, th = text_size(draw, text, font)
        draw.text((x + (w - tw) // 2, y + (h - th) // 2), text, fill="black", font=font)


def hline(draw, x1, x2, y, line_w):
    if x1 > x2:
        x1, x2 = x2, x1
    draw.line([(x1, y), (x2, y)], fill="black", width=line_w)


def vline(draw, x, y1, y2, line_w):
    if y1 > y2:
        y1, y2 = y2, y1
    draw.line([(x, y1), (x, y2)], fill="black", width=line_w)


def generate(topic_title: str, modules: list, caption_no: str = "2-1"):
    output = os.path.join(DIR, f"{topic_title}.png")
    caption = f"图 {caption_no} {topic_title}功能模块图"

    font_title = load_font(18)
    font_l2 = load_font(18)
    font_l3 = load_font(16)
    font_caption = load_font(18)
    line_w = s(2)

    l2_w, l2_h = s(130), s(42)
    l3_w, l3_h = s(34), s(118)
    l3_gap = s(8)
    col_gap = s(18)
    gap12 = s(30)
    gap23 = s(30)

    max_children = max(len(m["children"]) for m in modules)
    children_width = max_children * l3_w + (max_children - 1) * l3_gap
    col_width = max(l2_w, children_width)
    total_width = len(modules) * col_width + (len(modules) - 1) * col_gap

    margin_top = s(30)
    margin_bottom = s(70)
    title_h = s(48)
    title_y = margin_top
    l2_y = title_y + title_h + gap12
    l3_y = l2_y + l2_h + gap23

    tmp = Image.new("RGB", (1, 1), "white")
    tmp_draw = ImageDraw.Draw(tmp)
    title_w = text_size(tmp_draw, topic_title, font_title)[0] + s(40)
    title_w = max(title_w, total_width + s(40))

    img_w = max(total_width + s(80), title_w + s(40), MIN_WIDTH)
    img_h = max(l3_y + l3_h + margin_bottom, MIN_HEIGHT)

    img = Image.new("RGB", (img_w, img_h), "white")
    draw = ImageDraw.Draw(img)

    title_x = (img_w - title_w) // 2
    start_x = (img_w - total_width) // 2

    l2_boxes = []
    l3_boxes = []

    for module in modules:
        col_x = start_x + len(l2_boxes) * (col_width + col_gap)
        l2_x = col_x + (col_width - l2_w) // 2
        l2_boxes.append((l2_x, l2_y, l2_w, l2_h))

        child_count = len(module["children"])
        group_w = child_count * l3_w + (child_count - 1) * l3_gap
        group_x = col_x + (col_width - group_w) // 2
        boxes = []
        for child in module["children"]:
            x = group_x + len(boxes) * (l3_w + l3_gap)
            boxes.append((x, l3_y, l3_w, l3_h, child))
        l3_boxes.append(boxes)

    title_cx = title_x + title_w // 2
    title_bottom = title_y + title_h
    bus12_y = title_bottom + gap12 // 2

    vline(draw, title_cx, title_bottom, bus12_y, line_w)
    l2_tops = [(x + w // 2, y) for x, y, w, h in l2_boxes]
    hline(draw, l2_tops[0][0], l2_tops[-1][0], bus12_y, line_w)
    for cx, top in l2_tops:
        vline(draw, cx, bus12_y, top, line_w)

    for (l2_x, l2_y_pos, l2_w_val, l2_h_val), l3_group in zip(l2_boxes, l3_boxes):
        l2_cx = l2_x + l2_w_val // 2
        l2_bottom = l2_y_pos + l2_h_val
        bus23_y = l2_bottom + gap23 // 2
        l3_tops = [(x + w // 2, y) for x, y, w, h, _ in l3_group]

        vline(draw, l2_cx, l2_bottom, bus23_y, line_w)
        hline(draw, l3_tops[0][0], l3_tops[-1][0], bus23_y, line_w)
        for cx, top in l3_tops:
            vline(draw, cx, bus23_y, top, line_w)

    draw_box(draw, title_x, title_y, title_w, title_h, topic_title, font_title, line_w)
    for module, (x, y, w, h) in zip(modules, [(b[0], b[1], b[2], b[3]) for b in l2_boxes]):
        draw_box(draw, x, y, w, h, module["name"], font_l2, line_w)
    for group in l3_boxes:
        for x, y, w, h, text in group:
            draw_box(draw, x, y, w, h, text, font_l3, line_w, vertical=True)

    cap_w, cap_h = text_size(draw, caption, font_caption)
    draw.text(
        ((img_w - cap_w) // 2, img_h - cap_h - s(18)),
        caption,
        fill="black",
        font=font_caption,
    )

    img.save(output, "PNG", dpi=(OUTPUT_DPI, OUTPUT_DPI))
    print(f"Saved: {output} ({img_w}x{img_h}px, {OUTPUT_DPI}DPI, scale={SCALE}x)")
    return output


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("topic", type=int, choices=sorted(TOPICS.keys()), help="topic number")
    args = parser.parse_args()
    cfg = TOPICS[args.topic]
    generate(cfg["title"], cfg["modules"], f"2-{args.topic}")


if __name__ == "__main__":
    main()
