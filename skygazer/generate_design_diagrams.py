#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
SkyGazer功能模块层次结构图
- 用于第二章概要设计
- 展示7大核心模块及其子功能
"""

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, Rectangle
import os

plt.rcParams['font.sans-serif'] = ['Microsoft YaHei', 'SimHei', 'Arial Unicode MS']
plt.rcParams['axes.unicode_minus'] = False


def create_module_hierarchy():
    fig, ax = plt.subplots(1, 1, figsize=(20, 14))
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')
    fig.patch.set_facecolor('white')
    
    # 标题
    title_box = FancyBboxPatch((20, 92), 60, 6, boxstyle="round,pad=0.3",
                                facecolor='#2196F3', edgecolor='none')
    ax.add_patch(title_box)
    ax.text(50, 95, 'SkyGazer 功能模块层次结构图', 
            ha='center', va='center', fontsize=24, fontweight='bold', color='white')
    
    # 根节点
    root_box = FancyBboxPatch((30, 82), 40, 7, boxstyle="round,pad=0.3",
                               facecolor='#1976D2', edgecolor='white', linewidth=2)
    ax.add_patch(root_box)
    ax.text(50, 85.5, 'SkyGazer 智观天象 AI', 
            ha='center', va='center', fontsize=20, fontweight='bold', color='white')
    
    # 7大核心模块
    modules = [
        {
            'name': '用户管理模块',
            'color': '#4CAF50',
            'x': 2,
            'sub_modules': ['用户注册/登录', 'JWT认证授权', '个人信息管理', '健康档案维护']
        },
        {
            'name': '天气服务模块',
            'color': '#2196F3',
            'x': 15,
            'sub_modules': ['实时天气查询', '24小时预报', '7天趋势预报', '数据缓存优化']
        },
        {
            'name': 'AI智能模块 ⭐',
            'color': '#FF5722',
            'x': 28,
            'sub_modules': ['AI健康顾问对话', '用户画像解析', 'RAG知识检索', '风险评估算法']
        },
        {
            'name': '生活指数模块',
            'color': '#FF9800',
            'x': 41,
            'sub_modules': ['穿衣指数', '运动指数', '过敏指数', '洗车指数']
        },
        {
            'name': '预警推送模块',
            'color': '#F44336',
            'x': 54,
            'sub_modules': ['极端天气预警', '空气质量预警', '花粉浓度预警', '推送策略管理']
        },
        {
            'name': '数据可视化模块',
            'color': '#9C27B0',
            'x': 67,
            'sub_modules': ['天气趋势图表', '历史数据对比', 'PDF报表导出', '地图可视化']
        },
        {
            'name': '知识库管理模块',
            'color': '#607D8B',
            'x': 80,
            'sub_modules': ['医学知识录入', '向量数据存储', '语义检索优化', '知识更新维护']
        }
    ]
    
    # 绘制连接线
    for i, module in enumerate(modules):
        x_center = module['x'] + 6
        ax.plot([50, x_center], [82, 72], color='#BDBDBD', linewidth=2, zorder=1)
    
    # 绘制模块
    for module in modules:
        # 主模块框
        main_box = FancyBboxPatch((module['x'], 62), 12, 10, boxstyle="round,pad=0.3",
                                   facecolor=module['color'], edgecolor='white', linewidth=2)
        ax.add_patch(main_box)
        ax.text(module['x'] + 6, 67, module['name'], ha='center', va='center',
                fontsize=15, fontweight='bold', color='white', wrap=True)
        
        # 子模块
        for j, sub in enumerate(module['sub_modules']):
            sub_box = FancyBboxPatch((module['x'], 52 - j * 6), 12, 5, boxstyle="round,pad=0.2",
                                      facecolor='white', edgecolor=module['color'], linewidth=1.5)
            ax.add_patch(sub_box)
            ax.text(module['x'] + 6, 54.5 - j * 6, sub, ha='center', va='center',
                    fontsize=12, color='#333')
            
            # 连接线
            if j == 0:
                ax.plot([module['x'] + 6, module['x'] + 6], [62, 57], 
                       color=module['color'], linewidth=1.5, zorder=1)
            else:
                ax.plot([module['x'] + 6, module['x'] + 6], [52 - (j-1) * 6, 52 - j * 6 + 5], 
                       color=module['color'], linewidth=1.5, zorder=1)
    
    plt.tight_layout()
    return fig


def create_call_relationship():
    fig, ax = plt.subplots(1, 1, figsize=(18, 12))
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')
    fig.patch.set_facecolor('white')
    
    # 标题
    title_box = FancyBboxPatch((20, 92), 60, 6, boxstyle="round,pad=0.3",
                                facecolor='#673AB7', edgecolor='none')
    ax.add_patch(title_box)
    ax.text(50, 95, 'SkyGazer 核心模块调用关系图', 
            ha='center', va='center', fontsize=24, fontweight='bold', color='white')
    
    # 用户请求
    user_box = FancyBboxPatch((40, 80), 20, 6, boxstyle="round,pad=0.3",
                               facecolor='#FF9800', edgecolor='white', linewidth=2)
    ax.add_patch(user_box)
    ax.text(50, 83, '用户请求', ha='center', va='center',
            fontsize=18, fontweight='bold', color='white')
    
    # Controller层
    controller_box = FancyBboxPatch((10, 66), 80, 10, boxstyle="round,pad=0.3",
                                     facecolor='#2196F3', edgecolor='white', linewidth=2)
    ax.add_patch(controller_box)
    ax.text(50, 71, 'Controller层 (控制器)', ha='center', va='center',
            fontsize=18, fontweight='bold', color='white')
    
    controllers = ['WeatherController', 'AIController', 'UserController']
    for i, ctrl in enumerate(controllers):
        ax.text(25 + i * 25, 67, ctrl, ha='center', va='center',
                fontsize=14, color='#E3F2FD')
    
    # Service层
    services = [
        ('WeatherService', '#4CAF50', 15),
        ('AIService', '#FF5722', 40),
        ('UserService', '#9C27B0', 65)
    ]
    
    for name, color, x in services:
        box = FancyBboxPatch((x, 48), 20, 10, boxstyle="round,pad=0.3",
                              facecolor=color, edgecolor='white', linewidth=2)
        ax.add_patch(box)
        ax.text(x + 10, 53, name, ha='center', va='center',
                fontsize=16, fontweight='bold', color='white')
    
    # AI子服务
    ai_services = [
        ('QwenChatService', '#FF7043', 25),
        ('KnowledgeBaseService', '#FFA726', 55)
    ]
    
    for name, color, x in ai_services:
        box = FancyBboxPatch((x, 32), 20, 8, boxstyle="round,pad=0.3",
                              facecolor=color, edgecolor='white', linewidth=2)
        ax.add_patch(box)
        ax.text(x + 10, 36, name, ha='center', va='center',
                fontsize=14, fontweight='bold', color='white')
    
    # Repository层
    repo_box = FancyBboxPatch((10, 18), 80, 8, boxstyle="round,pad=0.3",
                               facecolor='#607D8B', edgecolor='white', linewidth=2)
    ax.add_patch(repo_box)
    ax.text(50, 22, 'Repository层 (数据访问层)', ha='center', va='center',
            fontsize=18, fontweight='bold', color='white')
    
    # 数据存储层
    databases = [
        ('MySQL', '#00796B', 15),
        ('Redis', '#D32F2F', 40),
        ('H2向量库', '#512DA8', 65)
    ]
    
    for name, color, x in databases:
        box = FancyBboxPatch((x, 4), 20, 8, boxstyle="round,pad=0.3",
                              facecolor=color, edgecolor='white', linewidth=2)
        ax.add_patch(box)
        ax.text(x + 10, 8, name, ha='center', va='center',
                fontsize=16, fontweight='bold', color='white')
    
    # 连接箭头
    arrow_style = dict(arrowstyle='->', color='#666', lw=2)
    
    # 用户 -> Controller
    ax.annotate('', xy=(50, 76), xytext=(50, 80), arrowprops=arrow_style)
    
    # Controller -> Service
    ax.annotate('', xy=(25, 58), xytext=(25, 66), arrowprops=arrow_style)
    ax.annotate('', xy=(50, 58), xytext=(50, 66), arrowprops=arrow_style)
    ax.annotate('', xy=(75, 58), xytext=(75, 66), arrowprops=arrow_style)
    
    # Service -> AI子服务
    ax.annotate('', xy=(35, 40), xytext=(45, 48), arrowprops=arrow_style)
    ax.annotate('', xy=(65, 40), xytext=(55, 48), arrowprops=arrow_style)
    
    # Service -> Repository
    ax.annotate('', xy=(50, 26), xytext=(50, 32), arrowprops=arrow_style)
    
    # Repository -> 数据库
    ax.annotate('', xy=(25, 12), xytext=(25, 18), arrowprops=arrow_style)
    ax.annotate('', xy=(50, 12), xytext=(50, 18), arrowprops=arrow_style)
    ax.annotate('', xy=(75, 12), xytext=(75, 18), arrowprops=arrow_style)
    
    plt.tight_layout()
    return fig


def main():
    output_dir = r'd:\jishe\diagrams2'
    os.makedirs(output_dir, exist_ok=True)
    
    print("=" * 80)
    print("概要设计架构图生成器")
    print("=" * 80)
    
    # 生成功能模块层次结构图
    print("\n[1/2] 正在生成功能模块层次结构图...")
    fig1 = create_module_hierarchy()
    fig1.savefig(os.path.join(output_dir, '概要设计_功能模块层次结构图.png'), 
                 dpi=200, bbox_inches='tight', facecolor='white', edgecolor='none')
    plt.close(fig1)
    print("   [OK] 功能模块层次结构图已保存")
    
    # 生成模块调用关系图
    print("\n[2/2] 正在生成模块调用关系图...")
    fig2 = create_call_relationship()
    fig2.savefig(os.path.join(output_dir, '概要设计_模块调用关系图.png'), 
                 dpi=200, bbox_inches='tight', facecolor='white', edgecolor='none')
    plt.close(fig2)
    print("   [OK] 模块调用关系图已保存")
    
    print("\n" + "=" * 80)
    print("✅ 概要设计架构图生成完成！")
    print("=" * 80)


if __name__ == '__main__':
    main()