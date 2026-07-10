#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
RAG知识增强流程图优化脚本
- 修复字体与框架重叠问题
- 放大字体提升可读性
"""

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, Circle
import os

plt.rcParams['font.sans-serif'] = ['Microsoft YaHei', 'SimHei', 'Arial Unicode MS']
plt.rcParams['axes.unicode_minus'] = False


def create_optimized_rag_flow():
    fig, ax = plt.subplots(1, 1, figsize=(24, 32))
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 160)
    ax.axis('off')
    fig.patch.set_facecolor('#FFFBEB')
    
    # 标题
    title_box = FancyBboxPatch((5, 153), 90, 5.5, boxstyle="round,pad=0.3",
                                facecolor='#EF4444', edgecolor='none', alpha=0.95)
    ax.add_patch(title_box)
    ax.text(50, 155.8, 'RAG 知识增强生成流程详解', 
            ha='center', va='center', fontsize=34, fontweight='bold', color='white')
    
    current_y = 147
    
    # 步骤1: 用户提问 - 增加高度和内边距
    step1_box = FancyBboxPatch((5, current_y - 6.5), 90, 6.5, boxstyle="round,pad=0.35",
                                facecolor='white', edgecolor='#3B82F6', linewidth=3)
    ax.add_patch(step1_box)
    
    step1_num = Circle((9, current_y - 3.25), 2, facecolor='#3B82F6', edgecolor='none')
    ax.add_patch(step1_num)
    ax.text(9, current_y - 3.25, '1', ha='center', va='center', fontsize=26, fontweight='bold', color='white')
    
    ax.text(14, current_y - 1.5, '用户提问', fontsize=23, fontweight='bold', color='#1E40AF')
    
    question_box = FancyBboxPatch((13, current_y - 6), 80, 3.2, boxstyle="round,pad=0.18",
                                   facecolor='#EFF6FF', edgecolor='#BFDBFE', linewidth=1.5)
    ax.add_patch(question_box)
    ax.text(53, current_y - 4.4, '"我是哮喘患者，明天去公园跑步合适吗？"', 
            ha='center', va='center', fontsize=18, color='#1E40AF', fontstyle='italic')
    
    current_y -= 8
    
    ax.annotate('', xy=(50, current_y + 0.6), xytext=(50, current_y + 1.5),
                arrowprops=dict(arrowstyle='->', color='#3B82F6', lw=3.5))
    
    # 步骤2: 问题理解 - 增加高度
    step2_box = FancyBboxPatch((5, current_y - 8), 90, 8, boxstyle="round,pad=0.35",
                                facecolor='white', edgecolor='#3B82F6', linewidth=3)
    ax.add_patch(step2_box)
    
    step2_num = Circle((9, current_y - 4), 2, facecolor='#3B82F6', edgecolor='none')
    ax.add_patch(step2_num)
    ax.text(9, current_y - 4, '2', ha='center', va='center', fontsize=26, fontweight='bold', color='white')
    
    ax.text(14, current_y - 1.2, '问题理解 - 提取关键信息', fontsize=22, fontweight='bold', color='#1E40AF')
    
    info_items = [
        ('用户画像', '哮喘患者 (特殊健康需求)'),
        ('活动场景', '户外跑步 (公园环境)'),
        ('时间维度', '明天 (短期预报范围)'),
        ('关注要素', '花粉浓度/空气质量/温湿度/风力')
    ]
    
    x_positions = [12, 34, 56, 78]
    for i, (title, desc) in enumerate(info_items):
        item_box = FancyBboxPatch((x_positions[i], current_y - 7), 19.5, 6, boxstyle="round,pad=0.18",
                                   facecolor='#DBEAFE', edgecolor='#3B82F6', linewidth=1.5)
        ax.add_patch(item_box)
        ax.text(x_positions[i] + 9.75, current_y - 2.8, title, ha='center', va='center', 
                fontsize=16, fontweight='bold', color='#1E40AF')
        ax.text(x_positions[i] + 9.75, current_y - 4.8, desc, ha='center', va='center', 
                fontsize=14, color='#3B82F6')
    
    current_y -= 10
    
    ax.annotate('', xy=(50, current_y + 0.6), xytext=(50, current_y + 1.5),
                arrowprops=dict(arrowstyle='->', color='#10B981', lw=3.5))
    
    # 步骤3: 多路数据获取 - 大幅增加高度
    step3_box = FancyBboxPatch((5, current_y - 11.5), 90, 11.5, boxstyle="round,pad=0.4",
                                facecolor='white', edgecolor='#10B981', linewidth=3)
    ax.add_patch(step3_box)
    
    step3_num = Circle((9, current_y - 5.75), 2, facecolor='#10B981', edgecolor='none')
    ax.add_patch(step3_num)
    ax.text(9, current_y - 5.75, '3', ha='center', va='center', fontsize=26, fontweight='bold', color='white')
    
    ax.text(14, current_y - 1, '多路数据获取 (并行处理)', fontsize=22, fontweight='bold', color='#065F46')
    
    # 知识库检索路径 - 增加高度
    kb_path = FancyBboxPatch((11, current_y - 10.5), 38, 8.5, boxstyle="round,pad=0.25",
                              facecolor='#ECFDF5', edgecolor='#059669', linewidth=2.5)
    ax.add_patch(kb_path)
    ax.text(30, current_y - 2.2, '知识库检索路径', ha='center', va='center', fontsize=17, fontweight='bold', color='#065F46')
    ax.text(13, current_y - 4, '① 问题向量化 (embedding-v3)', fontsize=15, color='#047857')
    ax.text(13, current_y - 6, '② 语义匹配 Top-K (K=5)', fontsize=15, color='#047857')
    
    result_box = FancyBboxPatch((13, current_y - 9.8), 34, 3, boxstyle="round,pad=0.18",
                                 facecolor='#D1FAE5', edgecolor='#10B981', linewidth=1.5)
    ax.add_patch(result_box)
    ax.text(30, current_y - 8.3, '检索结果: 天气预警 | 生活指数 | 活动建议 | 特殊人群健康', 
            ha='center', va='center', fontsize=13, color='#065F46')
    
    # 天气数据获取路径 - 增加高度
    wd_path = FancyBboxPatch((53, current_y - 10.5), 38, 8.5, boxstyle="round,pad=0.25",
                              facecolor='#EFF6FF', edgecolor='#2563EB', linewidth=2.5)
    ax.add_patch(wd_path)
    ax.text(72, current_y - 2.2, '天气数据获取路径', ha='center', va='center', fontsize=17, fontweight='bold', color='#1E40AF')
    ax.text(55, current_y - 4, '① 调用 WeatherService', fontsize=15, color='#1E40AF')
    ax.text(55, current_y - 6, '② 缓存优先策略 (Redis -> DB -> API)', fontsize=15, color='#1E40AF')
    
    weather_result = FancyBboxPatch((55, current_y - 9.8), 34, 3, boxstyle="round,pad=0.18",
                                     facecolor='#DBEAFE', edgecolor='#3B82F6', linewidth=1.5)
    ax.add_patch(weather_result)
    ax.text(72, current_y - 8.3, '天气数据: 温度22℃ | 湿度65% | AQI:85 | 花粉:高 | 风力:3级', 
            ha='center', va='center', fontsize=13, color='#1E40AF')
    
    current_y -= 13.5
    
    ax.annotate('', xy=(50, current_y + 0.6), xytext=(50, current_y + 1.5),
                arrowprops=dict(arrowstyle='->', color='#F59E0B', lw=3.5))
    
    # 步骤4: Prompt组装 - 增加高度
    step4_box = FancyBboxPatch((5, current_y - 9.5), 90, 9.5, boxstyle="round,pad=0.35",
                                facecolor='white', edgecolor='#F59E0B', linewidth=3)
    ax.add_patch(step4_box)
    
    step4_num = Circle((9, current_y - 4.75), 2, facecolor='#F59E0B', edgecolor='none')
    ax.add_patch(step4_num)
    ax.text(9, current_y - 4.75, '4', ha='center', va='center', fontsize=26, fontweight='bold', color='white')
    
    ax.text(14, current_y - 1, 'Prompt 组装 (上下文注入)', fontsize=22, fontweight='bold', color='#92400E')
    
    prompt_box = FancyBboxPatch((12, current_y - 9), 84, 7.5, boxstyle="round,pad=0.25",
                                 facecolor='#FFFBEB', edgecolor='#D97706', linewidth=2.5)
    ax.add_patch(prompt_box)
    
    prompt_lines = [
        ('【System Prompt】你是专业气象决策顾问...', '#92400E', 17, True),
        ('【Context - 天气】温度22℃, 湿度65%, AQI:85...', '#B45309', 15, False),
        ('【Context - 知识】哮喘患者应避免... [引用x3]', '#B45309', 15, False),
        ('【User】我是哮喘患者，明天去公园跑步合适吗？', '#DC2626', 17, True)
    ]
    
    for i, (text, color, size, bold) in enumerate(prompt_lines):
        y_pos = current_y - 1.8 - i * 1.8
        weight = 'bold' if bold else 'normal'
        ax.text(15, y_pos, text, fontsize=size, color=color, fontweight=weight)
    
    current_y -= 11
    
    ax.annotate('', xy=(50, current_y + 0.6), xytext=(50, current_y + 1.5),
                arrowprops=dict(arrowstyle='->', color='#EF4444', lw=3.5))
    
    # 步骤5: LLM生成 - 增加高度
    step5_box = FancyBboxPatch((5, current_y - 7.5), 90, 7.5, boxstyle="round,pad=0.35",
                                facecolor='white', edgecolor='#EF4444', linewidth=3)
    ax.add_patch(step5_box)
    
    step5_num = Circle((9, current_y - 3.75), 2, facecolor='#EF4444', edgecolor='none')
    ax.add_patch(step5_num)
    ax.text(9, current_y - 3.75, '5', ha='center', va='center', fontsize=26, fontweight='bold', color='white')
    
    ax.text(14, current_y - 1, 'LLM 生成回答', fontsize=22, fontweight='bold', color='#991B1B')
    
    llm_model = FancyBboxPatch((12, current_y - 7), 38, 5.5, boxstyle="round,pad=0.25",
                                facecolor='#FEE2E2', edgecolor='#DC2626', linewidth=2.5)
    ax.add_patch(llm_model)
    ax.text(31, current_y - 3, '通义千问 qwen-plus', ha='center', va='center',
            fontsize=17, fontweight='bold', color='#991B1B')
    ax.text(31, current_y - 5, 'temperature=0.7 | max_tokens=2000', ha='center', va='center',
            fontsize=15, color='#DC2626')
    
    sse_box = FancyBboxPatch((54, current_y - 7), 39, 5.5, boxstyle="round,pad=0.25",
                              facecolor='#FCE7F3', edgecolor='#DB2777', linewidth=2.5)
    ax.add_patch(sse_box)
    ax.text(73.5, current_y - 3, 'SSE 流式输出', ha='center', va='center',
            fontsize=17, fontweight='bold', color='#9D174D')
    ax.text(73.5, current_y - 5, '实时返回流式内容，提升用户体验', ha='center', va='center',
            fontsize=14, color='#BE185D')
    
    current_y -= 9
    
    ax.annotate('', xy=(50, current_y + 0.6), xytext=(50, current_y + 1.5),
                arrowprops=dict(arrowstyle='->', color='#8B5CF6', lw=3.5))
    
    # 步骤6: 结果后处理 - 增加高度
    step6_box = FancyBboxPatch((5, current_y - 11.5), 90, 11.5, boxstyle="round,pad=0.35",
                                facecolor='white', edgecolor='#8B5CF6', linewidth=3)
    ax.add_patch(step6_box)
    
    step6_num = Circle((9, current_y - 5.75), 2, facecolor='#8B5CF6', edgecolor='none')
    ax.add_patch(step6_num)
    ax.text(9, current_y - 5.75, '6', ha='center', va='center', fontsize=26, fontweight='bold', color='white')
    
    ax.text(14, current_y - 1, '结果后处理与返回', fontsize=22, fontweight='bold', color='#581C87')
    
    post_items = [
        ('引用标注处理', '在回答中标记知识来源，增强可信度'),
        ('安全过滤', '过滤敏感/不当内容，确保合规性'),
        ('日志记录', '记录完整交互日志支持后续分析'),
        ('Redis缓存', '缓存结果避免重复计算（TTL:24h）')
    ]
    
    for i, (title, desc) in enumerate(post_items):
        item_box = FancyBboxPatch((11 + i * 21, current_y - 9), 19.5, 7, boxstyle="round,pad=0.18",
                                   facecolor='#F3E8FF', edgecolor='#A855F7', linewidth=1.5)
        ax.add_patch(item_box)
        ax.text(20.75 + i * 21, current_y - 3.5, title, ha='center', va='center',
                fontsize=16, fontweight='bold', color='#7C3AED')
        ax.text(20.75 + i * 21, current_y - 6.2, desc, ha='center', va='center',
                fontsize=13, color='#6D28D9', linespacing=1.3)
    
    current_y -= 13
    
    # 最终输出框 - 增加高度
    output_box = FancyBboxPatch((5, current_y - 8), 90, 8, boxstyle="round,pad=0.35",
                                 facecolor='#ECFDF5', edgecolor='#059669', linewidth=3)
    ax.add_patch(output_box)
    ax.text(50, current_y - 1.5, '最终输出给用户', ha='center', va='center',
            fontsize=21, fontweight='bold', color='#065F46')
    
    final_answer = FancyBboxPatch((8, current_y - 7.5), 84, 6, boxstyle="round,pad=0.25",
                                   facecolor='#D1FAE5', edgecolor='#10B981', linewidth=2)
    ax.add_patch(final_answer)
    
    answer_text = '''考虑到您65岁的年龄和哮喘病史，结合北京明天高花粉浓度(AQI:85)和轻度污染的气象条件，
强烈建议您取消明天的户外晨练计划。推荐替代方案：室内太极拳(20分钟)或下午3点后使用空气净化器进行拉伸运动。
[参考来源：医学建议x2 + 气象数据x1 + 安全指南x1]'''
    ax.text(50, current_y - 4.5, answer_text, ha='center', va='center',
            fontsize=15, color='#047857', linespacing=1.4)
    
    plt.tight_layout()
    return fig


def main():
    output_dir = r'd:\jishe\diagrams2'
    os.makedirs(output_dir, exist_ok=True)
    
    print("=" * 80)
    print("RAG流程图优化脚本")
    print("=" * 80)
    
    print("\n[正在生成优化的RAG知识增强流程图...")
    fig = create_optimized_rag_flow()
    fig.savefig(os.path.join(output_dir, '03_RAG知识增强流程图_优化版.png'), dpi=250, bbox_inches='tight',
                 facecolor=fig.get_facecolor(), edgecolor='none')
    fig.savefig(os.path.join(output_dir, '03_RAG知识增强流程图_优化版.pdf'), format='pdf', bbox_inches='tight',
                 facecolor=fig.get_facecolor(), edgecolor='none')
    plt.close(fig)
    print("   [OK] 03_RAG知识增强流程图_优化版 已保存")
    
    print("\n" + "=" * 80)
    print(f"优化完成！")
    print("=" * 80)


if __name__ == '__main__':
    main()