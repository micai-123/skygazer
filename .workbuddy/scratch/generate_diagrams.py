# -*- coding: utf-8 -*-
"""Generate 7 plain-style UML/diagram PNGs for SkyGazer weather system."""
from PIL import Image, ImageDraw, ImageFont
import os

OUT_DIR = r'D:/MyProject/jishe2'
FONT = r'C:/Windows/Fonts/simsun.ttc'
FONT_BOLD = r'C:/Windows/Fonts/simhei.ttf'

def ft(size, bold=False):
    return ImageFont.truetype(FONT_BOLD if bold else FONT, size)

def text_size(draw, text, font):
    return draw.textbbox((0, 0), text, font=font)[2:]

def draw_text_center(draw, pos, text, font, fill='black'):
    w, h = text_size(draw, text, font)
    x = pos[0] - w // 2
    y = pos[1] - h // 2
    draw.text((x, y), text, font=font, fill=fill)

def draw_rect(draw, x, y, w, h, text, font, fill='white', border='black', bold_title=False):
    draw.rectangle([x, y, x+w, y+h], fill=fill, outline=border, width=2)
    if text:
        draw_text_center(draw, (x+w//2, y+h//2), text, font)

def draw_arrow(draw, x1, y1, x2, y2, dashed=False, label=None, font=None, fill='black'):
    if dashed:
        # draw dashed line
        total = ((x2-x1)**2 + (y2-y1)**2) ** 0.5
        if total == 0: return
        dx, dy = (x2-x1)/total, (y2-y1)/total
        step = 8
        cur = 0
        while cur < total:
            s = cur
            e = min(cur + step, total)
            draw.line([(x1+dx*s, y1+dy*s), (x1+dx*e, y1+dy*e)], fill=fill, width=2)
            cur += 2*step
    else:
        draw.line([(x1, y1), (x2, y2)], fill=fill, width=2)
    # arrowhead at (x2,y2)
    angle = 0
    if x2 != x1 or y2 != y1:
        import math
        angle = math.atan2(y2-y1, x2-x1)
    ah = 10
    a1 = (x2 - ah*math.cos(angle - 0.35), y2 - ah*math.sin(angle - 0.35))
    a2 = (x2 - ah*math.cos(angle + 0.35), y2 - ah*math.sin(angle + 0.35))
    draw.polygon([(x2, y2), a1, a2], fill=fill)
    if label and font:
        lw, lh = text_size(draw, label, font)
        mx, my = (x1+x2)//2, (y1+y2)//2
        # draw white background small box for label
        draw.rectangle([mx-lw//2-2, my-lh//2-2, mx+lw//2+2, my+lh//2+2], fill='white', outline=None)
        draw.text((mx-lw//2, my-lh//2), label, font=font, fill=fill)

def draw_oval(draw, x, y, w, h, text, font, fill='white', border='black'):
    draw.ellipse([x, y, x+w, y+h], fill=fill, outline=border, width=2)
    if text:
        draw_text_center(draw, (x+w//2, y+h//2), text, font)

def draw_diamond(draw, cx, cy, size, text, font, fill='white', border='black'):
    pts = [(cx, cy-size), (cx+size, cy), (cx, cy+size), (cx-size, cy)]
    draw.polygon(pts, fill=fill, outline=border)
    if text:
        draw_text_center(draw, (cx, cy-size//2-2), text, font)

def draw_stick_figure(draw, x, y, size, label, font):
    # head
    draw.ellipse([x-size//3, y-size, x+size//3, y-size//3], outline='black', width=2)
    # body
    draw.line([(x, y-size//3), (x, y+size//2)], fill='black', width=2)
    # arms
    draw.line([(x-size//2, y), (x+size//2, y)], fill='black', width=2)
    # legs
    draw.line([(x, y+size//2), (x-size//2, y+size)], fill='black', width=2)
    draw.line([(x, y+size//2), (x+size//2, y+size)], fill='black', width=2)
    # label
    lw, lh = text_size(draw, label, font)
    draw.text((x-lw//2, y+size+5), label, font=font, fill='black')

def save(img, name):
    path = os.path.join(OUT_DIR, name)
    img.save(path)
    print('saved', path)

# ---------- 1. 系统模块关系图 ----------
def make_module_relation():
    W, H = 1600, 950
    img = Image.new('RGB', (W, H), 'white')
    draw = ImageDraw.Draw(img)
    f_title = ft(24, True)
    f_mod = ft(18, True)
    f_sub = ft(15)

    # top
    top_text = 'SkyGazer 智能天气决策系统'
    tw, th = text_size(draw, top_text, f_title)
    top_x, top_y = W//2 - tw//2 - 10, 60
    top_w, top_h = tw + 40, th + 24
    draw_rect(draw, top_x, top_y, top_w, top_h, top_text, f_title)

    modules = [
        ('用户管理模块', ['注册/登录', 'JWT认证', '个人资料']),
        ('天气服务模块', ['实时天气', '逐时预报', '七日预报', '空气质量']),
        ('AI智能模块', ['多轮对话', '工具调用', 'RAG检索', '图像识别']),
        ('生活指数模块', ['穿衣指数', '运动指数', '过敏指数', '洗车指数']),
        ('预警推送模块', ['极端天气', '空气预警', '花粉预警']),
        ('数据可视化模块', ['图表报表', '天气地图', '省市钻取']),
        ('知识库管理模块', ['知识录入', '向量检索', '更新维护']),
    ]
    n = len(modules)
    mod_w, mod_h = 150, 50
    gap = 35
    row_y = 240
    total_w = n*mod_w + (n-1)*gap
    start_x = (W - total_w) // 2
    mod_positions = []
    for i, (mod, subs) in enumerate(modules):
        x = start_x + i*(mod_w+gap)
        draw_rect(draw, x, row_y, mod_w, mod_h, mod, f_mod)
        mod_positions.append((x+mod_w//2, row_y+mod_h, subs))
        # top to module
        draw.line([(top_x+top_w//2, top_y+top_h), (x+mod_w//2, row_y)], fill='black', width=2)

    # sub boxes in 2 rows to avoid overlap
    sub_w, sub_h = 95, 36
    sub_gap = 15
    row_gap = 55
    for cx, bottom_y, subs in mod_positions:
        n_sub = len(subs)
        n_row1 = (n_sub + 1) // 2
        n_row2 = n_sub - n_row1
        gy = bottom_y + 80
        # connector trunk
        trunk_y = gy - 25
        draw.line([(cx, bottom_y), (cx, trunk_y)], fill='black', width=2)
        # helper to center a row of k items
        def row_positions(k):
            if k == 0: return []
            rw = k*sub_w + (k-1)*sub_gap
            rx = cx - rw//2
            return [rx + j*(sub_w+sub_gap) for j in range(k)]
        row1_xs = row_positions(n_row1)
        row2_xs = row_positions(n_row2)
        # horizontal connector for row1
        if row1_xs:
            draw.line([(row1_xs[0]+sub_w//2, trunk_y), (row1_xs[-1]+sub_w//2, trunk_y)], fill='black', width=2)
        # draw rows
        for rx in row1_xs:
            draw_rect(draw, rx, gy, sub_w, sub_h, subs[row1_xs.index(rx)], f_sub)
            draw.line([(rx+sub_w//2, trunk_y), (rx+sub_w//2, gy)], fill='black', width=2)
        if row2_xs:
            gy2 = gy + sub_h + row_gap
            rw2 = n_row2*sub_w + (n_row2-1)*sub_gap
            cx2 = cx
            # connect row1 center to row2 center
            draw.line([(cx, trunk_y), (cx, gy2-25)], fill='black', width=2)
            draw.line([(row2_xs[0]+sub_w//2, gy2-25), (row2_xs[-1]+sub_w//2, gy2-25)], fill='black', width=2)
            for rx in row2_xs:
                idx = n_row1 + row2_xs.index(rx)
                draw_rect(draw, rx, gy2, sub_w, sub_h, subs[idx], f_sub)
                draw.line([(rx+sub_w//2, gy2-25), (rx+sub_w//2, gy2)], fill='black', width=2)
    return img

# ---------- 2. 用例图 ----------
def make_use_case():
    W, H = 1300, 1000
    img = Image.new('RGB', (W, H), 'white')
    draw = ImageDraw.Draw(img)
    f_actor = ft(18)
    f_uc = ft(16)
    f_rel = ft(14)

    # actors
    draw_stick_figure(draw, 150, 400, 45, '普通用户', f_actor)
    draw_stick_figure(draw, 150, 800, 45, '管理员', f_actor)

    use_cases = [
        ('注册/登录', 600, 140),
        ('查询天气', 600, 230),
        ('AI气象问答', 600, 320),
        ('天气图像识别', 600, 410),
        ('查看生活指数', 600, 500),
        ('查看气象预警', 600, 590),
        ('查看天气地图', 600, 680),
        ('维护用户\n与知识库', 950, 760),
        ('修改个人信息', 950, 860),
    ]
    ovals = {}
    for text, x, y in use_cases:
        w, h = text_size(draw, text.replace('\n',''), f_uc)
        ow, oh = max(w+44, 130), max(h+34, 64)
        ovals[text] = (x, y, ow, oh)
        draw_oval(draw, x-ow//2, y-oh//2, ow, oh, text, f_uc)

    # associations
    user_cases = ['注册/登录','查询天气','AI气象问答','天气图像识别','查看生活指数','查看气象预警','查看天气地图']
    for text in user_cases:
        x, y, ow, oh = ovals[text]
        draw.line([(210, 430), (x-ow//2, y)], fill='black', width=2)
    x, y, ow, oh = ovals['维护用户\n与知识库']
    draw.line([(210, 860), (x-ow//2, y)], fill='black', width=2)

    # extend: 修改个人信息 <<extend>> 维护用户与知识库
    x1, y1, ow1, oh1 = ovals['修改个人信息']
    x2, y2, ow2, oh2 = ovals['维护用户\n与知识库']
    draw_arrow(draw, x1-ow1//2, y1, x2+ow2//2, y2, dashed=True, label='<<extend>>', font=f_rel)
    return img

# ---------- 3. 系统功能架构设计图 ----------
def make_architecture():
    W, H = 1500, 750
    img = Image.new('RGB', (W, H), 'white')
    draw = ImageDraw.Draw(img)
    f_top = ft(24, True)
    f_sub = ft(18, True)

    top_text = 'SkyGazer 天气预测与查询系统'
    tw, th = text_size(draw, top_text, f_top)
    top_x, top_y = W//2 - tw//2 - 10, 70
    top_w, top_h = tw + 40, th + 24
    draw_rect(draw, top_x, top_y, top_w, top_h, top_text, f_top)

    modules = ['用户管理子系统','天气服务子系统','AI智能子系统','生活指数子系统','预警推送子系统','数据可视化子系统','知识库管理子系统']
    n = len(modules)
    box_w, box_h = 150, 50
    gap = 25
    row_y = 280
    total_w = n*box_w + (n-1)*gap
    start_x = (W - total_w) // 2
    for i, m in enumerate(modules):
        x = start_x + i*(box_w+gap)
        draw_rect(draw, x, row_y, box_w, box_h, m, f_sub)
        # dashed arrow from top to box
        draw_arrow(draw, top_x+top_w//2, top_y+top_h, x+box_w//2, row_y, dashed=True)
    return img

# ---------- 4. 子系统功能 ----------
def make_subsystem():
    W, H = 1200, 700
    img = Image.new('RGB', (W, H), 'white')
    draw = ImageDraw.Draw(img)
    f_top = ft(24, True)
    f_func = ft(20, True)

    top_text = '天气服务子系统'
    tw, th = text_size(draw, top_text, f_top)
    top_x, top_y = W//2 - tw//2 - 10, 80
    top_w, top_h = tw + 40, th + 24
    draw_rect(draw, top_x, top_y, top_w, top_h, top_text, f_top)

    funcs = ['实时天气查询','逐小时预报','七日趋势预报','空气质量查询']
    n = len(funcs)
    box_w, box_h = 190, 55
    gap = 35
    row_y = 300
    total_w = n*box_w + (n-1)*gap
    start_x = (W - total_w) // 2
    for i, f in enumerate(funcs):
        x = start_x + i*(box_w+gap)
        draw_rect(draw, x, row_y, box_w, box_h, f, f_func)
        draw_arrow(draw, top_x+top_w//2, top_y+top_h, x+box_w//2, row_y)
    return img

# ---------- 5. 功能类图 ----------
def make_class():
    W, H = 1400, 900
    img = Image.new('RGB', (W, H), 'white')
    draw = ImageDraw.Draw(img)
    f_title = ft(20, True)
    f_attr = ft(16)
    f_meth = ft(16)

    classes = [
        ('WeatherController', ['+getWeatherByCity()', '+getAirQuality()', '+analyzeWeather()'], 120, 120),
        ('WeatherService', ['+queryWeather()', '+cacheWeather()', '+fallbackWeather()'], 520, 120),
        ('WeatherMapper', ['+selectByCity()', '+insert()', '+update()'], 920, 120),
        ('WeatherData', ['-city: String', '-temp: Double', '-humidity: Integer', '-updateTime: Date',
                         '+toDTO()', '+isFresh()'], 520, 480),
    ]
    box_w = 280
    for name, members, x, y in classes:
        header_h = 40
        row_h = 28
        h = header_h + len(members)*row_h + 10
        # header
        draw.rectangle([x, y, x+box_w, y+header_h], fill='white', outline='black', width=2)
        draw_text_center(draw, (x+box_w//2, y+header_h//2), name, f_title)
        # body
        draw.rectangle([x, y+header_h, x+box_w, y+h], fill='white', outline='black', width=2)
        for i, m in enumerate(members):
            draw.text((x+10, y+header_h+5+i*row_h), m, font=f_attr, fill='black')

    # relationships
    draw_arrow(draw, 120+box_w, 120+header_h//2, 520, 120+header_h//2)
    draw_arrow(draw, 520+box_w, 120+header_h//2, 920, 120+header_h//2)
    draw_arrow(draw, 520+box_w//2, 120+header_h+len(classes[1][1])*row_h+10, 520+box_w//2, 480)
    # labels
    draw.text((300, 90), '调用', font=ft(14), fill='black')
    draw.text((700, 90), '调用', font=ft(14), fill='black')
    draw.text((540, 310), '映射为', font=ft(14), fill='black')
    return img

# ---------- 6. 时序图 ----------
def make_sequence():
    W, H = 1600, 950
    img = Image.new('RGB', (W, H), 'white')
    draw = ImageDraw.Draw(img)
    f_obj = ft(17, True)
    f_msg = ft(15)

    objects = ['用户','前端页面','天气服务','Redis缓存','和风天气API']
    n = len(objects)
    top_y = 80
    lifeline_h = 700
    x_positions = []
    box_w, box_h = 140, 45
    gap = 240
    start_x = 120
    for i, obj in enumerate(objects):
        x = start_x + i*gap
        x_positions.append(x + box_w//2)
        draw.rectangle([x, top_y, x+box_w, top_y+box_h], fill='white', outline='black', width=2)
        draw_text_center(draw, (x+box_w//2, top_y+box_h//2), obj, f_obj)
        # lifeline
        for yy in range(top_y+box_h, top_y+box_h+lifeline_h, 10):
            draw.line([(x+box_w//2, yy), (x+box_w//2, min(yy+5, top_y+box_h+lifeline_h))], fill='black', width=1)

    messages = [
        (0, 1, '1: 选择城市'),
        (1, 2, '2: 请求天气'),
        (2, 3, '3: 查询缓存'),
        (3, 2, '4: 缓存未命中'),
        (2, 4, '5: 调用气象API'),
        (4, 2, '6: 返回天气数据'),
        (2, 3, '7: 写入缓存'),
        (2, 1, '8: 返回结构化数据'),
        (1, 0, '9: 渲染展示'),
    ]
    y = top_y + box_h + 60
    for src, dst, label in messages:
        x1, x2 = x_positions[src], x_positions[dst]
        draw_arrow(draw, x1, y, x2, y)
        lw, lh = text_size(draw, label, f_msg)
        draw.text(((x1+x2)//2 - lw//2, y-lh-4), label, font=f_msg, fill='black')
        y += 70
    return img

# ---------- 7. 实体关系图 ----------
def make_er():
    W, H = 1500, 1000
    img = Image.new('RGB', (W, H), 'white')
    draw = ImageDraw.Draw(img)
    f_ent = ft(17, True)
    f_attr = ft(14)
    f_rel = ft(15)

    entities = [
        ('用户', ['user_id','username','password','profile'], 180, 150),
        ('城市', ['city_id','city_name','adcode'], 650, 150),
        ('天气记录', ['record_id','weather_json','update_time'], 1120, 150),
        ('会话', ['session_id','conversation_id','messages'], 180, 580),
        ('知识库条目', ['entry_id','question','answer','embedding'], 650, 580),
        ('预警记录', ['alert_id','alert_type','content','level'], 1120, 580),
    ]
    box_w = 170
    for name, attrs, x, y in entities:
        header_h = 34
        row_h = 24
        h = header_h + len(attrs)*row_h + 6
        draw.rectangle([x, y, x+box_w, y+header_h], fill='white', outline='black', width=2)
        draw_text_center(draw, (x+box_w//2, y+header_h//2), name, f_ent)
        draw.rectangle([x, y+header_h, x+box_w, y+h], fill='white', outline='black', width=2)
        for i, a in enumerate(attrs):
            draw.text((x+8, y+header_h+4+i*row_h), a, font=f_attr, fill='black')

    rels = [
        (180+box_w//2, 150+header_h, '拥有', 1, 'N', 180+box_w//2, 580, '会话'),
        (180+box_w, 150+header_h//2, '关注', 1, 'N', 650, 150+header_h//2, '城市'),
        (650+box_w, 150+header_h//2, '产生', 1, 'N', 1120, 150+header_h//2, '天气记录'),
        (650+box_w//2, 150+header_h, '触发', 1, 'N', 1120+box_w//2, 580, '预警记录'),
        (180+box_w//2, 580+header_h, '引用', 1, 'N', 650, 580+header_h//2, '知识库条目'),
        (650+box_w//2, 580+header_h, '检索', 1, 'N', 650+box_w//2, 580+header_h, ''),  # self? skip
    ]
    for x1,y1,label,c1,c2,x2,y2,ent in rels:
        if not ent: continue
        mx, my = (x1+x2)//2, (y1+y2)//2
        draw_diamond(draw, mx, my, 24, label, f_rel)
        draw.line([(x1, y1), (mx, my-24)], fill='black', width=2)
        draw.line([(mx, my+24), (x2, y2)], fill='black', width=2)
        # cardinality
        draw.text((x1+5, y1-18), str(c1), font=f_rel, fill='black')
        draw.text((x2-20, y2-18), str(c2), font=f_rel, fill='black')
    return img

if __name__ == '__main__':
    os.makedirs(OUT_DIR, exist_ok=True)
    save(make_module_relation(), '图3-1_系统模块关系图.png')
    save(make_use_case(), '图5-1_系统总用例图.png')
    save(make_architecture(), '图8-2_系统功能架构设计图.png')
    save(make_subsystem(), '图8-3_子系统功能图.png')
    save(make_class(), '图8-5_功能类图.png')
    save(make_sequence(), '图8-6_天气查询时序图.png')
    save(make_er(), '图8-7_实体关系图.png')
    print('done')
