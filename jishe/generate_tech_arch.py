#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
SkyGazer项目分层技术架构图生成器
- 模仿参考图的左侧标签+右侧内容布局
- 6层架构：应用功能层→前端展示层→后端服务层→AI智能层→数据存储层→基础设施层
"""

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, Rectangle
import numpy as np
import os

plt.rcParams['font.sans-serif'] = ['Microsoft YaHei', 'SimHei', 'Arial Unicode MS']
plt.rcParams['axes.unicode_minus'] = False


def create_tech_stack_architecture():
    fig, ax = plt.subplots(1, 1, figsize=(22, 18))
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')
    fig.patch.set_facecolor('white')
    
    # ========== 第1层：应用功能层 (浅蓝色) ==========
    layer1_bg = FancyBboxPatch((12, 85), 86, 13, boxstyle="round,pad=0.3",
                                facecolor='#E3F2FD', edgecolor='#1976D2', linewidth=3)
    ax.add_patch(layer1_bg)
    
    layer1_label = FancyBboxPatch((0.5, 85), 10.5, 13, boxstyle="round,pad=0.25",
                                   facecolor='#1976D2', edgecolor='none')
    ax.add_patch(layer1_label)
    ax.text(5.75, 91.5, '应\n用\n功\n能\n层', ha='center', va='center',
            fontsize=16, fontweight='bold', color='white', linespacing=1.4)
    
    # 功能模块 - 左侧
    funcs_left = [
        ('智能天气查询', '#BBDEFB'),
        ('AI健康顾问对话', '#BBDEFB'),
        ('生活指数推荐', '#BBDEFB'),
        ('用户个性化设置', '#BBDEFB')
    ]
    
    for i, (text, color) in enumerate(funcs_left):
        box = FancyBboxPatch((14, 96 - i*2.75), 36, 2.45, boxstyle="round,pad=0.15",
                              facecolor=color, edgecolor='#1565C0', linewidth=1.5)
        ax.add_patch(box)
        ax.text(32, 97.23 - i*2.75, text, ha='center', va='center',
                fontsize=16, fontweight='bold', color='#0D47A1')
    
    # 功能模块 - 右侧
    funcs_right = [
        ('风险预警推送', '#C8E6C9'),
        ('数据可视化报表', '#C8E6C9'),
        ('知识库管理', '#C8E6C9'),
        ('多模态交互', '#C8E6C9')
    ]
    
    for i, (text, color) in enumerate(funcs_right):
        box = FancyBboxPatch((53, 96 - i*2.75), 43, 2.45, boxstyle="round,pad=0.15",
                              facecolor=color, edgecolor='#388E3C', linewidth=1.5)
        ax.add_patch(box)
        ax.text(74.5, 97.23 - i*2.75, text, ha='center', va='center',
                fontsize=16, fontweight='bold', color='#1B5E20')
    
    # ========== 第2层：前端展示层 (浅黄色) ==========
    layer2_bg = FancyBboxPatch((12, 72), 86, 11.5, boxstyle="round,pad=0.3",
                                facecolor='#FFFDE7', edgecolor='#F9A825', linewidth=3)
    ax.add_patch(layer2_bg)
    
    layer2_label = FancyBboxPatch((0.5, 72), 10.5, 11.5, boxstyle="round,pad=0.25",
                                   facecolor='#1976D2', edgecolor='none')
    ax.add_patch(layer2_label)
    ax.text(5.75, 77.75, '前\n端\n展\n示\n层', ha='center', va='center',
            fontsize=16, fontweight='bold', color='white', linespacing=1.4)
    
    # 前端技术栈
    frontend_tech = [
        [('Vue 3', '#FFF9C4'), ('Vite 5', '#FFF9C4')],
        [('ECharts 5', '#FFF9C4'), ('Axios', '#FFF9C4')]
    ]
    
    for row_idx, row in enumerate(frontend_tech):
        for col_idx, (tech, color) in enumerate(row):
            x_pos = 15 + col_idx * 19
            y_pos = 81 - row_idx * 3.2
            box = FancyBboxPatch((x_pos, y_pos), 17, 2.8, boxstyle="round,pad=0.15",
                                  facecolor=color, edgecolor='#F57F17', linewidth=1.5)
            ax.add_patch(box)
            ax.text(x_pos + 8.5, y_pos + 1.4, tech, ha='center', va='center',
                    fontsize=17, fontweight='bold', color='#E65100')
    
    # 前端框架Logo区域
    framework_box = FancyBboxPatch((55, 74), 41, 8.5, boxstyle="round,pad=0.2",
                                    facecolor='white', edgecolor='#F9A825', linewidth=2)
    ax.add_patch(framework_box)
    ax.text(75.5, 80.5, 'Vue Router 4', ha='center', va='center',
            fontsize=19, fontweight='bold', color='#42b883')
    ax.text(60, 76.5, 'Pinia', ha='center', va='center',
            fontsize=17, fontweight='bold', color='#FFD54F')
    ax.text(89, 76.5, 'jsPDF', ha='center', va='center',
            fontsize=17, fontweight='bold', color='#FF7043')
    
    # ========== 第3层：后端服务层 (白色/浅灰) ==========
    layer3_bg = FancyBboxPatch((12, 59), 86, 11.5, boxstyle="round,pad=0.3",
                                facecolor='#FAFAFA', edgecolor='#757575', linewidth=3)
    ax.add_patch(layer3_bg)
    
    layer3_label = FancyBboxPatch((0.5, 59), 10.5, 11.5, boxstyle="round,pad=0.25",
                                   facecolor='#1976D2', edgecolor='none')
    ax.add_patch(layer3_label)
    ax.text(5.75, 64.75, '后\n端\n服\n务\n层', ha='center', va='center',
            fontsize=16, fontweight='bold', color='white', linespacing=1.4)
    
    # Spring Boot
    spring_box = FancyBboxPatch((14, 62), 22, 6, boxstyle="round,pad=0.2",
                                 facecolor='white', edgecolor='#6DB33F', linewidth=2)
    ax.add_patch(spring_box)
    ax.text(25, 65, 'Spring Boot', ha='center', va='center',
            fontsize=19, fontweight='bold', color='#6DB33F')
    ax.text(25, 63, '3.2.5', ha='center', va='center',
            fontsize=14, color='#666')
    
    # REST API
    rest_box = FancyBboxPatch((39, 62), 24, 6, boxstyle="round,pad=0.2",
                               facecolor='white', edgecolor='#1976D2', linewidth=2)
    ax.add_patch(rest_box)
    ax.text(51, 65, 'RESTful API', ha='center', va='center',
            fontsize=18, fontweight='bold', color='#1976D2')
    ax.text(51, 63, 'WebFlux + Security', ha='center', va='center',
            fontsize=13, color='#666')
    
    # 数据管理
    data_mgmt = FancyBboxPatch((66, 62), 29, 6, boxstyle="round,pad=0.2",
                                facecolor='white', edgecolor='#00ACC1', linewidth=2)
    ax.add_patch(data_mgmt)
    ax.text(80.5, 65, '数据管理', ha='center', va='center',
            fontsize=18, fontweight='bold', color='#00ACC1')
    ax.text(80.5, 63, 'JPA + Validation', ha='center', va='center',
            fontsize=13, color='#666')
    
    # JWT认证
    jwt_box = FancyBboxPatch((14, 61), 81, 2.5, boxstyle="round,pad=0.15",
                              facecolor='#FFF3E0', edgecolor='#FF9800', linewidth=1.5)
    ax.add_patch(jwt_box)
    ax.text(54.5, 62.25, 'JWT Authentication + Rate Limiting', ha='center', va='center',
            fontsize=15, fontweight='bold', color='#E65100')
    
    # ========== 第4层：AI智能层 (浅紫色) ==========
    layer4_bg = FancyBboxPatch((12, 46), 86, 11.5, boxstyle="round,pad=0.3",
                                facecolor='#F3E5F5', edgecolor='#7B1FA2', linewidth=3)
    ax.add_patch(layer4_bg)
    
    layer4_label = FancyBboxPatch((0.5, 46), 10.5, 11.5, boxstyle="round,pad=0.25",
                                   facecolor='#1976D2', edgecolor='none')
    ax.add_patch(layer4_label)
    ax.text(5.75, 51.75, 'A I\n智\n能\n层', ha='center', va='center',
            fontsize=16, fontweight='bold', color='white', linespacing=1.4)
    
    # AI技术栈
    ai_techs = [
        ('Spring AI', '1.0.0-M4', '#CE93D8', '#4A148C'),
        ('Qwen LLM', '通义千问', '#B39DDB', '#4A148C'),
        ('RAG检索', '向量相似度', '#D1C4E9', '#4A148C'),
        ('Embedding', '文本向量化', '#E1BEE7', '#4A148C')
    ]
    
    for i, (name, desc, bg_color, text_color) in enumerate(ai_techs):
        x_pos = 14 + i * 21
        box = FancyBboxPatch((x_pos, 49), 19, 7.5, boxstyle="round,pad=0.2",
                              facecolor=bg_color, edgecolor=text_color, linewidth=2)
        ax.add_patch(box)
        ax.text(x_pos + 9.5, 54.5, name, ha='center', va='center',
                fontsize=17, fontweight='bold', color=text_color)
        ax.text(x_pos + 9.5, 51.5, desc, ha='center', va='center',
                fontsize=13, color='#666')
    
    # ========== 第5层：数据存储层 (浅绿色) ==========
    layer5_bg = FancyBboxPatch((12, 34), 86, 10.5, boxstyle="round,pad=0.3",
                                facecolor='#E8F5E9', edgecolor='#388E3C', linewidth=3)
    ax.add_patch(layer5_bg)
    
    layer5_label = FancyBboxPatch((0.5, 34), 10.5, 10.5, boxstyle="round,pad=0.25",
                                   facecolor='#1976D2', edgecolor='none')
    ax.add_patch(layer5_label)
    ax.text(5.75, 39.25, '数\n据\n存\n储\n层', ha='center', va='center',
            fontsize=16, fontweight='bold', color='white', linespacing=1.4)
    
    # MySQL
    mysql_box = FancyBboxPatch((14, 35.5), 40, 8, boxstyle="round,pad=0.25",
                                facecolor='#C8E6C9', edgecolor='#00796B', linewidth=2.5)
    ax.add_patch(mysql_box)
    ax.text(34, 41, 'MySQL', ha='center', va='center',
            fontsize=20, fontweight='bold', color='#00796B')
    ax.text(34, 37.5, '用户数据 | 天气历史 | 交互日志', ha='center', va='center',
            fontsize=14, color='#1B5E20')
    
    # Redis
    redis_box = FancyBboxPatch((57, 35.5), 39, 8, boxstyle="round,pad=0.25",
                                facecolor='#DCEDC8', edgecolor='#689F38', linewidth=2.5)
    ax.add_patch(redis_box)
    ax.text(76.5, 41, 'Redis', ha='center', va='center',
            fontsize=20, fontweight='bold', color='#689F38')
    ax.text(76.5, 37.5, '缓存层 | 会话存储 | 限流计数器', ha='center', va='center',
            fontsize=14, color='#33691E')
    
    # 向量数据库标注
    vector_text = ax.text(55, 34.2, 'Vector Knowledge Base (H2)', ha='center', va='top',
                          fontsize=13, fontweight='bold', color='#558B2F', style='italic')
    
    # ========== 第6层：基础设施层 (多彩) ==========
    layer6_bg = FancyBboxPatch((12, 22), 86, 10.5, boxstyle="round,pad=0.3",
                                facecolor='#ECEFF1', edgecolor='#607D8B', linewidth=3)
    ax.add_patch(layer6_bg)
    
    layer6_label = FancyBboxPatch((0.5, 22), 10.5, 10.5, boxstyle="round,pad=0.25",
                                   facecolor='#1976D2', edgecolor='none')
    ax.add_patch(layer6_label)
    ax.text(5.75, 27.25, '基\n础\n设\n施\n层', ha='center', va='center',
            fontsize=16, fontweight='bold', color='white', linespacing=1.4)
    
    # 多色块
    infra_items = [
        ('容器化部署', '#FFCCBC', '#BF360C'),
        ('服务器', '#BBDEFB', '#0D47A1'),
        ('缓存系统', '#C8E6C9', '#1B5E20'),
        ('网络通信', '#E1BEE7', '#4A148C')
    ]
    
    for i, (text, bg_color, border_color) in enumerate(infra_items):
        x_pos = 14 + i * 21.5
        box = FancyBboxPatch((x_pos, 24), 20, 7, boxstyle="round,pad=0.2",
                              facecolor=bg_color, edgecolor=border_color, linewidth=2.5)
        ax.add_patch(box)
        ax.text(x_pos + 10, 27.5, text, ha='center', va='center',
                fontsize=17, fontweight='bold', color=border_color)
        
        # 添加具体技术说明
        if i == 0:
            ax.text(x_pos + 10, 25.3, 'Docker + Nginx', ha='center', va='center',
                    fontsize=12, color='#666')
        elif i == 1:
            ax.text(x_pos + 10, 25.3, 'Linux / Cloud', ha='center', va='center',
                    fontsize=12, color='#666')
        elif i == 2:
            ax.text(x_pos + 10, 25.3, 'Redis Cluster', ha='center', va='center',
                    fontsize=12, color='#666')
        elif i == 3:
            ax.text(x_pos + 10, 25.3, 'HTTP/HTTPS', ha='center', va='center',
                    fontsize=12, color='#666')
    
    # 标题
    title_box = FancyBboxPatch((20, 97), 60, 2.5, boxstyle="round,pad=0.2",
                                facecolor='#1976D2', edgecolor='none')
    ax.add_patch(title_box)
    ax.text(50, 98.25, 'SkyGazer 智观天象 AI 技术架构全景图', 
            ha='center', va='center', fontsize=26, fontweight='bold', color='white')
    
    plt.tight_layout()
    return fig


def main():
    output_dir = r'd:\jishe\diagrams2'
    os.makedirs(output_dir, exist_ok=True)
    
    print("=" * 80)
    print("SkyGazer 技术架构图生成器")
    print("=" * 80)
    
    print("\n正在生成分层技术架构图...")
    fig = create_tech_stack_architecture()
    
    output_png = os.path.join(output_dir, '07_技术架构全景图.png')
    output_pdf = os.path.join(output_dir, '07_技术架构全景图.pdf')
    
    fig.savefig(output_png, dpi=250, bbox_inches='tight',
                 facecolor=fig.get_facecolor(), edgecolor='none')
    fig.savefig(output_pdf, format='pdf', bbox_inches='tight',
                 facecolor=fig.get_facecolor(), edgecolor='none')
    plt.close(fig)
    
    print(f"   [OK] PNG: {output_png}")
    print(f"   [OK] PDF: {output_pdf}")
    
    print("\n" + "=" * 80)
    print("✅ 技术架构图生成完成！")
    print("=" * 80)


if __name__ == '__main__':
    main()