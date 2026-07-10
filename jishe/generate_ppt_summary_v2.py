#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
SkyGazer技术框架PPT章节总结页（无emoji版本）
- 构成技术架构章节的第一页
- 包含核心亮点、技术栈、设计原则
"""

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, Rectangle, Circle
import os

plt.rcParams['font.sans-serif'] = ['Microsoft YaHei', 'SimHei', 'Arial Unicode MS']
plt.rcParams['axes.unicode_minus'] = False


def create_ppt_chapter_summary():
    fig, ax = plt.subplots(1, 1, figsize=(16, 10))
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')
    fig.patch.set_facecolor('#1A1A2E')
    
    # ========== 顶部装饰条 ==========
    top_bar = FancyBboxPatch((0, 92), 100, 8, boxstyle="round,pad=0",
                              facecolor='#0F3460', edgecolor='none')
    ax.add_patch(top_bar)
    
    ax.text(50, 95.5, '第 3 章 技术架构设计', 
            ha='center', va='center', fontsize=20, color='#E94560', fontweight='bold')
    
    # ========== 主标题区域 ==========
    title_bg = FancyBboxPatch((10, 62), 80, 27, boxstyle="round,pad=0.4",
                               facecolor='#16213E', edgecolor='#533483', linewidth=3)
    ax.add_patch(title_bg)
    
    ax.text(50, 82, 'SkyGazer 技术框架', 
            ha='center', va='center', fontsize=38, fontweight='bold', color='#00D9FF')
    
    ax.text(50, 74, '智观天象 AI - 基于Spring AI的多模态智能天气预测与决策系统', 
            ha='center', va='center', fontsize=18, color='#A8E6CF')
    
    # ========== 核心技术亮点卡片 ==========
    highlights = [
        ('AI能力', 'Spring AI + Qwen LLM\nRAG检索增强\n向量数据库', '#FF6B6B'),
        ('高性能', 'WebFlux响应式\nRedis集群缓存\n异步任务处理', '#4ECDC4'),
        ('安全可靠', 'JWT认证\nAPI限流\n数据加密', '#45B7D1'),
        ('现代化', 'Docker容器化\nVite快速构建\nCI/CD友好', '#96CEB4')
    ]
    
    for i, (title, desc, color) in enumerate(highlights):
        x_pos = 8 + i * 22
        box = FancyBboxPatch((x_pos, 40), 20, 18, boxstyle="round,pad=0.3",
                              facecolor=color, edgecolor='white', linewidth=2, alpha=0.9)
        ax.add_patch(box)
        ax.text(x_pos + 10, 54, title, ha='center', va='center',
                fontsize=18, fontweight='bold', color='#1A1A2E')
        ax.text(x_pos + 10, 46, desc, ha='center', va='center',
                fontsize=13, color='#1A1A2E', linespacing=1.4)
    
    # ========== 技术栈概览 ==========
    tech_stack_bg = FancyBboxPatch((10, 8), 80, 29, boxstyle="round,pad=0.4",
                                    facecolor='#0F3460', edgecolor='#E94560', linewidth=2.5)
    ax.add_patch(tech_stack_bg)
    
    ax.text(15, 32, '技术栈', ha='left', va='center',
            fontsize=18, fontweight='bold', color='#00D9FF')
    
    # 前端
    frontend_items = ['Vue 3', 'Vite 5', 'ECharts 5', 'Pinia']
    for i, tech in enumerate(frontend_items):
        x_pos = 22 + i * 13
        circle = Circle((x_pos, 26), 4.5, facecolor='#4ECDC4', edgecolor='white', linewidth=1.5)
        ax.add_patch(circle)
        ax.text(x_pos, 26, tech, ha='center', va='center',
                fontsize=14, fontweight='bold', color='#1A1A2E')
    
    # 后端
    backend_items = ['Spring Boot', 'Spring AI', 'MySQL', 'Redis']
    for i, tech in enumerate(backend_items):
        x_pos = 22 + i * 13
        circle = Circle((x_pos, 18), 4.5, facecolor='#FF6B6B', edgecolor='white', linewidth=1.5)
        ax.add_patch(circle)
        ax.text(x_pos, 18, tech, ha='center', va='center',
                fontsize=14, fontweight='bold', color='#1A1A2E')
    
    # 设计原则
    ax.text(85, 26, '设计原则', ha='right', va='center',
            fontsize=16, fontweight='bold', color='#A8E6CF')
    
    principles = [
        '单一职责 • 依赖倒置',
        '高内聚 • 低耦合',
        '可扩展 • 可维护',
        '安全 • 高性能'
    ]
    
    for i, principle in enumerate(principles):
        ax.text(55, 22 - i * 3.5, f'• {principle}', ha='left', va='center',
                fontsize=14, color='#E9E3E6')
    
    # ========== 底部页码 ==========
    ax.text(95, 3, '3-1', ha='right', va='center',
            fontsize=14, color='#666')
    
    plt.tight_layout()
    return fig


def main():
    output_dir = r'd:\jishe\diagrams2'
    os.makedirs(output_dir, exist_ok=True)
    
    print("=" * 80)
    print("PPT章节总结页生成器")
    print("=" * 80)
    
    print("\n正在生成技术架构章节第一页...")
    fig = create_ppt_chapter_summary()
    
    output_png = os.path.join(output_dir, 'PPT_技术架构_第1页_总结.png')
    output_pdf = os.path.join(output_dir, 'PPT_技术架构_第1页_总结.pdf')
    
    fig.savefig(output_png, dpi=200, bbox_inches='tight',
                 facecolor=fig.get_facecolor(), edgecolor='none')
    fig.savefig(output_pdf, format='pdf', bbox_inches='tight',
                 facecolor=fig.get_facecolor(), edgecolor='none')
    plt.close(fig)
    
    print(f"   [OK] PNG: {output_png}")
    print(f"   [OK] PDF: {output_pdf}")
    
    print("\n" + "=" * 80)
    print("✅ PPT章节总结页生成完成！")
    print("=" * 80)


if __name__ == '__main__':
    main()