#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
SkyGazer项目架构图生成器 V2
- 生成智能决策流程图和函数调用流程图
- 优化原有01-04架构图（字体放大+结构紧凑）
"""

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, Circle
import numpy as np
import os

plt.rcParams['font.sans-serif'] = ['Microsoft YaHei', 'SimHei', 'Arial Unicode MS']
plt.rcParams['axes.unicode_minus'] = False
plt.rcParams['font.size'] = 14

COLORS = {
    'primary': '#3B82F6',
    'secondary': '#8B5CF6',
    'success': '#10B981',
    'warning': '#F59E0B',
    'danger': '#EF4444',
    'info': '#6366F1',
}

def create_intelligent_decision_flow():
    fig, ax = plt.subplots(1, 1, figsize=(24, 32))
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 130)
    ax.axis('off')
    fig.patch.set_facecolor('#FAFAFA')
    
    title_box = FancyBboxPatch((5, 124), 90, 4.5, boxstyle="round,pad=0.3",
                                facecolor='#C62828', edgecolor='none', alpha=0.95)
    ax.add_patch(title_box)
    ax.text(50, 126.2, 'SkyGazer 智能决策流程图（RAG + 风险评估）', 
            ha='center', va='center', fontsize=28, fontweight='bold', color='white')
    
    layer1_box = FancyBboxPatch((3, 88), 30, 33, boxstyle="round,pad=0.4",
                                 facecolor='#FCE4EC', edgecolor='#C62828', linewidth=3)
    ax.add_patch(layer1_box)
    ax.text(18, 119, '第一层：数据输入', ha='center', va='center', 
            fontsize=20, fontweight='bold', color='#B71C1C')
    
    i1_box = FancyBboxPatch((5, 103), 26, 14, boxstyle="round,pad=0.25",
                              facecolor='white', edgecolor='#E91E63', linewidth=2)
    ax.add_patch(i1_box)
    ax.text(18, 115, '用户画像', ha='center', va='center', fontsize=16, fontweight='bold', color='#AD1457')
    profile_data = [('年龄:', '65岁'), ('健康状况:', '哮喘患者'), ('活动偏好:', '晨练跑步'), ('敏感因素:', '寒冷/花粉')]
    for i, (label, value) in enumerate(profile_data):
        ax.text(8, 111.5 - i*2, label, fontsize=13, color='#333')
        ax.text(15, 111.5 - i*2, value, fontsize=13, fontweight='bold', color='#C62828')
    
    i2_box = FancyBboxPatch((5, 95), 26, 7, boxstyle="round,pad=0.2",
                              facecolor='#FFF9C4', edgecolor='#F9A825', linewidth=2)
    ax.add_patch(i2_box)
    ax.text(18, 100.5, '用户问题', ha='center', va='center', fontsize=16, fontweight='bold', color='#F57F17')
    ax.text(18, 97.5, '"明天适合户外运动吗？"', ha='center', va='center', fontsize=14, color='#333', style='italic')
    
    ax.annotate('', xy=(18, 102), xytext=(18, 103),
                arrowprops=dict(arrowstyle='->', color='#E91E63', lw=2))
    
    i3_box = FancyBboxPatch((5, 81), 26, 12.5, boxstyle="round,pad=0.25",
                              facecolor='#E3F2FD', edgecolor='#1565C0', linewidth=2)
    ax.add_patch(i3_box)
    ax.text(18, 91.5, '实时天气', ha='center', va='center', fontsize=16, fontweight='bold', color='#0D47A1')
    weather_data = [('气温:', '15°C'), ('降水概率:', '80%'), ('空气质量:', 'AQI 120'), ('花粉浓度:', '高')]
    for i, (label, value) in enumerate(weather_data):
        ax.text(8, 88 - i*1.8, label, fontsize=12, color='#333')
        ax.text(16, 88 - i*1.8, value, fontsize=12, fontweight='bold', color='#1565C0')
    
    ax.annotate('', xy=(18, 80.5), xytext=(18, 94.5),
                arrowprops=dict(arrowstyle='->', color='#F9A825', lw=2))
    
    i4_box = FancyBboxPatch((5, 89), 26, 5, boxstyle="round,pad=0.2",
                              facecolor='#E8F5E9', edgecolor='#2E7D32', linewidth=2)
    ax.add_patch(i4_box)
    ax.text(18, 92, '知识库检索', ha='center', va='center', fontsize=15, fontweight='bold', color='#1B5E20')
    ax.text(18, 90, '哮喘患者应避免高花粉...', ha='center', va='center', fontsize=11, color='#333', style='italic')
    
    layer2_box = FancyBboxPatch((35, 58), 30, 63, boxstyle="round,pad=0.4",
                                 facecolor='#FFF9C4', edgecolor='#F9A825', linewidth=3)
    ax.add_patch(layer2_box)
    ax.text(50, 119.5, '第二层：智能处理', ha='center', va='center', 
            fontsize=20, fontweight='bold', color='#F57F17')
    
    p1_box = FancyBboxPatch((37, 105), 26, 12.5, boxstyle="round,pad=0.25",
                              facecolor='white', edgecolor='#FF9800', linewidth=2)
    ax.add_patch(p1_box)
    ax.text(50, 115.5, '画像解析引擎', ha='center', va='center', fontsize=15, fontweight='bold', color='#E65100')
    p1_items = ['JSON反序列化处理', '字段校验与补全', '风险因子提取', '生成结构化对象']
    for i, item in enumerate(p1_items):
        ax.text(39, 112.5 - i*1.8, f'• {item}', fontsize=11, color='#333')
    
    p2_box = FancyBboxPatch((37, 90), 26, 13.5, boxstyle="round,pad=0.25",
                              facecolor='#FFEBEE', edgecolor='#D32F2F', linewidth=2)
    ax.add_patch(p2_box)
    ax.text(50, 101.5, '风险评估算法', ha='center', va='center', fontsize=15, fontweight='bold', color='#C62828')
    ax.text(50, 99, 'RiskScore = 权重 x 因素 x 敏感系数', ha='center', va='center', fontsize=11, color='#333')
    risk_items = [('温度风险:', '4.5分'), ('空气质量风险:', '5.6分'), ('降水风险:', '2.88分'), ('总风险值:', '13.0 -> HIGH')]
    for i, (label, value) in enumerate(risk_items):
        ax.text(40, 96 - i*1.5, label, fontsize=11, color='#333')
        color = '#C62828' if 'HIGH' in value else '#333'
        weight = 'bold' if 'HIGH' in value else 'normal'
        ax.text(56, 96 - i*1.5, value, fontsize=11, color=color, fontweight=weight)
    
    p3_box = FancyBboxPatch((37, 74), 26, 14.5, boxstyle="round,pad=0.25",
                              facecolor='#E8EAF6', edgecolor='#3F51B5', linewidth=2)
    ax.add_patch(p3_box)
    ax.text(50, 86.5, 'Prompt动态组装', ha='center', va='center', fontsize=15, fontweight='bold', color='#303F9F')
    prompt_items = ['【系统角色】专业气象健康顾问', '【用户特征】65岁 + 哮喘 + 晨练', '【环境数据】15C + 高污染 + 大雨', '【知识支撑】医学建议 + 安全指南']
    for i, item in enumerate(prompt_items):
        ax.text(39, 83.5 - i*2, item, fontsize=10, color='#333')
    
    p4_box = FancyBboxPatch((37, 59), 26, 13, boxstyle="round,pad=0.25",
                              facecolor='#E0F2F1', edgecolor='#00796B', linewidth=2)
    ax.add_patch(p4_box)
    ax.text(50, 70, 'AI模型推理', ha='center', va='center', fontsize=15, fontweight='bold', color='#00695C')
    ai_items = ['通义千问Plus 处理', '多维信息融合分析', '个性化建议生成', '结构化JSON输出']
    for i, item in enumerate(ai_items):
        ax.text(39, 66.5 - i*1.8, f'• {item}', fontsize=11, color='#333')
    
    ax.annotate('', xy=(50, 103.5), xytext=(50, 104.5),
                arrowprops=dict(arrowstyle='->', color='#FF9800', lw=2))
    ax.annotate('', xy=(50, 88.5), xytext=(50, 89.5),
                arrowprops=dict(arrowstyle='->', color='#D32F2F', lw=2))
    ax.annotate('', xy=(50, 72.5), xytext=(50, 73.5),
                arrowprops=dict(arrowstyle='->', color='#3F51B5', lw=2))
    
    layer3_box = FancyBboxPatch((67, 58), 30, 63, boxstyle="round,pad=0.4",
                                 facecolor='#E0F2F1', edgecolor='#00695C', linewidth=3)
    ax.add_patch(layer3_box)
    ax.text(82, 119.5, '第三层：决策输出', ha='center', va='center', 
            fontsize=20, fontweight='bold', color='#004D40')
    
    o1_box = FancyBboxPatch((69, 107), 26, 10.5, boxstyle="round,pad=0.25",
                              facecolor='#FFCDD2', edgecolor='#B71C1C', linewidth=2)
    ax.add_patch(o1_box)
    ax.text(82, 115.5, '风险等级判定', ha='center', va='center', fontsize=15, fontweight='bold', color='#B71C1C')
    ax.text(82, 112.5, 'HIGH RISK', ha='center', va='center', fontsize=18, fontweight='bold', color='#C62828')
    ax.text(82, 109.5, '综合评分超过阈值10分', ha='center', va='center', fontsize=11, color='#333')
    
    o2_box = FancyBboxPatch((69, 93), 26, 12.5, boxstyle="round,pad=0.25",
                              facecolor='#FFF9C4', edgecolor='#F57F17', linewidth=2)
    ax.add_patch(o2_box)
    ax.text(82, 103.5, '核心决策建议', ha='center', va='center', fontsize=15, fontweight='bold', color='#E65100')
    advice_text = '考虑到您65岁的年龄和哮喘病史,\n结合当前高花粉浓度和轻度污染,\n强烈建议取消明天的户外晨练计划。'
    ax.text(82, 98, advice_text, ha='center', va='center', fontsize=11, color='#333', linespacing=1.4, style='italic')
    
    o3_box = FancyBboxPatch((69, 79), 26, 12.5, boxstyle="round,pad=0.25",
                              facecolor='#E8F5E9', edgecolor='#388E3C', linewidth=2)
    ax.add_patch(o3_box)
    ax.text(82, 89.5, '替代方案推荐', ha='center', va='center', fontsize=15, fontweight='bold', color='#2E7D32')
    alternatives = ['方案一: 室内太极拳 (20分钟)', '方案二: 下午3点后室内快走', '方案三: 使用空气净化器后拉伸']
    for i, alt in enumerate(alternatives):
        ax.text(71, 85.5 - i*2, f'[OK] {alt}', fontsize=11, color='#2E7D32')
    
    o4_box = FancyBboxPatch((69, 60), 26, 17.5, boxstyle="round,pad=0.25",
                              facecolor='#E3F2FD', edgecolor='#1976D2', linewidth=2)
    ax.add_patch(o4_box)
    ax.text(82, 75.5, '数据溯源与缓存', ha='center', va='center', fontsize=15, fontweight='bold', color='#0D47A1')
    trace_data = [('知识来源ID:', 'kb_20260411_001'), ('天气数据时间:', '2026-04-11 14:30'), ('Redis缓存TTL:', '24小时'), ('响应耗时:', '1.8秒')]
    for i, (label, value) in enumerate(trace_data):
        ax.text(71, 71.5 - i*2.5, label, fontsize=11, color='#333')
        ax.text(83, 71.5 - i*2.5, value, fontsize=11, fontweight='bold', color='#1565C0')
    
    ax.annotate('', xy=(82, 105.5), xytext=(82, 106.5),
                arrowprops=dict(arrowstyle='->', color='#B71C1C', lw=2))
    ax.annotate('', xy=(82, 91.5), xytext=(82, 92.5),
                arrowprops=dict(arrowstyle='->', color='#F57F17', lw=2))
    ax.annotate('', xy=(82, 77.5), xytext=(82, 78.5),
                arrowprops=dict(arrowstyle='->', color='#388E3C', lw=2))
    
    ax.annotate('', xy=(36, 110), xytext=(33, 110),
                arrowprops=dict(arrowstyle='->', color='#666', lw=3, connectionstyle="arc3,rad=0"))
    ax.annotate('', xy=(67, 110), xytext=(64, 110),
                arrowprops=dict(arrowstyle='->', color='#666', lw=3, connectionstyle="arc3,rad=0"))
    
    bottom_box = FancyBboxPatch((5, 52), 90, 4.5, boxstyle="round,pad=0.2",
                                 facecolor='#ECEFF1', edgecolor='#607D8B', linewidth=1.5)
    ax.add_patch(bottom_box)
    ax.text(50, 54.2, '核心价值: 基于用户画像 + 实时气象 + 医学知识库的多维智能决策系统', 
            ha='center', va='center', fontsize=15, color='#37474F', fontweight='bold')
    
    plt.tight_layout()
    return fig


def create_function_calling_flow():
    fig, ax = plt.subplots(1, 1, figsize=(22, 18))
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 80)
    ax.axis('off')
    fig.patch.set_facecolor('#FAFAFA')
    
    title_box = FancyBboxPatch((5, 73), 90, 5, boxstyle="round,pad=0.3",
                                facecolor='#EF6C00', edgecolor='none', alpha=0.95)
    ax.add_patch(title_box)
    ax.text(50, 75.5, 'Spring AI 函数调用（Function Calling）流程图', 
            ha='center', va='center', fontsize=26, fontweight='bold', color='white')
    
    step1_box = FancyBboxPatch((5, 53), 28, 17, boxstyle="round,pad=0.4",
                                facecolor='#E8F5E9', edgecolor='#2E7D32', linewidth=3)
    ax.add_patch(step1_box)
    
    s1_header = FancyBboxPatch((6, 67), 26, 2.5, boxstyle="round,pad=0.15",
                                facecolor='#2E7D32', edgecolor='none')
    ax.add_patch(s1_header)
    ax.text(19, 68.2, '步骤一：意图识别', ha='center', va='center', fontsize=17, fontweight='bold', color='white')
    
    q_box = FancyBboxPatch((7, 59), 24, 6.5, boxstyle="round,pad=0.2",
                             facecolor='white', edgecolor='#4CAF50', linewidth=1.5)
    ax.add_patch(q_box)
    ax.text(19, 63.5, '用户提问', ha='center', va='center', fontsize=14, fontweight='bold', color='#2E7D32')
    ax.text(19, 61, '"北京今天多少度？"', ha='center', va='center', fontsize=13, color='#333', style='italic')
    
    analysis_box = FancyBboxPatch((7, 54.5), 24, 3.5, boxstyle="round,pad=0.15",
                                   facecolor='#C8E6C9', edgecolor='#43A047', linewidth=1.5)
    ax.add_patch(analysis_box)
    ax.text(19, 56.5, 'Spring AI 分析: 识别需要获取实时温度数据', ha='center', va='center', fontsize=12, color='#1B5E20')
    
    ax.annotate('', xy=(19, 58), xytext=(19, 59),
                arrowprops=dict(arrowstyle='->', color='#2E7D32', lw=2))
    
    step2_box = FancyBboxPatch((36, 46), 28, 24, boxstyle="round,pad=0.4",
                                facecolor='#FFF3E0', edgecolor='#EF6C00', linewidth=3)
    ax.add_patch(step2_box)
    
    s2_header = FancyBboxPatch((37, 67.5), 26, 2.5, boxstyle="round,pad=0.15",
                                facecolor='#EF6C00', edgecolor='none')
    ax.add_patch(s2_header)
    ax.text(50, 68.7, '步骤二：函数调用', ha='center', va='center', fontsize=17, fontweight='bold', color='white')
    
    fc_steps = [
        ('选择函数', "getWeatherData(city)", '#FFE0B2'),
        ('提取参数', "city = '北京'", '#FFE0B2'),
        ('执行函数', '调用和风天气API', '#FFCC80'),
        ('返回真实数据', "{temp:25, humidity:60}", '#FFB74D')
    ]
    
    y_pos = 64
    for i, (title, content, color) in enumerate(fc_steps):
        fc_item = FancyBboxPatch((38, y_pos - 4), 24, 3.8, boxstyle="round,pad=0.15",
                                  facecolor=color, edgecolor='#E65100', linewidth=1.5)
        ax.add_patch(fc_item)
        ax.text(50, y_pos - 1.8, title, ha='center', va='center', fontsize=13, fontweight='bold', color='#E65100')
        
        if i == 3:
            ax.text(50, y_pos - 3.2, content, ha='center', va='center', fontsize=11,
                   color='#BF360C', family='Consolas', fontweight='bold')
        elif i == 0 or i == 1:
            ax.text(50, y_pos - 3.2, content, ha='center', va='center', fontsize=11,
                   color='#333', family='Consolas')
        else:
            ax.text(50, y_pos - 3.2, content, ha='center', va='center', fontsize=11, color='#333')
        
        if i < 3:
            ax.annotate('', xy=(50, y_pos - 4.2), xytext=(50, y_pos - 4.5),
                       arrowprops=dict(arrowstyle='->', color='#EF6C00', lw=1.5))
        
        y_pos -= 4.5
    
    step3_box = FancyBboxPatch((67, 53), 28, 17, boxstyle="round,pad=0.4",
                                facecolor='#E3F2FD', edgecolor='#1565C0', linewidth=3)
    ax.add_patch(step3_box)
    
    s3_header = FancyBboxPatch((68, 67), 26, 2.5, boxstyle="round,pad=0.15",
                                facecolor='#1565C0', edgecolor='none')
    ax.add_patch(s3_header)
    ax.text(81, 68.2, '步骤三：答案生成', ha='center', va='center', fontsize=17, fontweight='bold', color='white')
    
    prompt_box = FancyBboxPatch((69, 59), 24, 6.5, boxstyle="round,pad=0.2",
                                 facecolor='white', edgecolor='#2196F3', linewidth=1.5)
    ax.add_patch(prompt_box)
    ax.text(81, 63.5, '数据注入Prompt', ha='center', va='center', fontsize=14, fontweight='bold', color='#0D47A1')
    ax.text(81, 61, '将API结果嵌入上下文', ha='center', va='center', fontsize=12, color='#333')
    
    llm_box = FancyBboxPatch((69, 54.5), 24, 3.5, boxstyle="round,pad=0.15",
                               facecolor='#BBDEFB', edgecolor='#1976D2', linewidth=1.5)
    ax.add_patch(llm_box)
    ax.text(81, 56.5, 'LLM生成回答: 基于真实数据输出', ha='center', va='center', fontsize=12, color='#0D47A1')
    
    result_box = FancyBboxPatch((70, 49), 22, 4.5, boxstyle="round,pad=0.2",
                                 facecolor='#E8F5E9', edgecolor='#4CAF50', linewidth=2)
    ax.add_patch(result_box)
    ax.text(81, 51.5, '"北京今日气温25°C"', ha='center', va='center',
            fontsize=14, fontweight='bold', color='#2E7D32', style='italic')
    
    ax.annotate('', xy=(81, 58), xytext=(81, 59),
                arrowprops=dict(arrowstyle='->', color='#1565C0', lw=2))
    ax.annotate('', xy=(81, 53.5), xytext=(81, 54.5),
                arrowprops=dict(arrowstyle='->', color='#1976D2', lw=2))
    
    ax.annotate('', xy=(35.5, 62), xytext=(33, 62),
                arrowprops=dict(arrowstyle='->', color='#666', lw=3.5, connectionstyle="arc3,rad=0"))
    ax.annotate('', xy=(66.5, 62), xytext=(64, 62),
                arrowprops=dict(arrowstyle='->', color='#666', lw=3.5, connectionstyle="arc3,rad=0"))
    
    bottom_box = FancyBboxPatch((5, 42), 90, 5, boxstyle="round,pad=0.2",
                                 facecolor='#ECEFF1', edgecolor='#607D8B', linewidth=1.5)
    ax.add_patch(bottom_box)
    ax.text(50, 44.5, '核心优势: AI自动选择并执行合适的工具函数，确保回答基于真实数据而非幻觉', 
            ha='center', va='center', fontsize=14, color='#37474F', fontweight='bold')
    
    plt.tight_layout()
    return fig


def main():
    output_dir = r'd:\jishe\diagrams2'
    os.makedirs(output_dir, exist_ok=True)
    
    print("=" * 80)
    print("SkyGazer 架构图生成器 V2")
    print("=" * 80)
    
    print("\n[任务一] 生成智能决策流程图和函数调用流程图")
    print("-" * 60)
    
    print("\n[1/2] 正在生成智能决策流程图...")
    fig1 = create_intelligent_decision_flow()
    fig1.savefig(os.path.join(output_dir, '05_智能决策流程图.png'), dpi=250, bbox_inches='tight',
                 facecolor=fig1.get_facecolor(), edgecolor='none')
    fig1.savefig(os.path.join(output_dir, '05_智能决策流程图.pdf'), format='pdf', bbox_inches='tight',
                 facecolor=fig1.get_facecolor(), edgecolor='none')
    plt.close(fig1)
    print("   [OK] 智能决策流程图已保存")
    
    print("\n[2/2] 正在生成函数调用流程图...")
    fig2 = create_function_calling_flow()
    fig2.savefig(os.path.join(output_dir, '06_函数调用流程图.png'), dpi=250, bbox_inches='tight',
                 facecolor=fig2.get_facecolor(), edgecolor='none')
    fig2.savefig(os.path.join(output_dir, '06_函数调用流程图.pdf'), format='pdf', bbox_inches='tight',
                 facecolor=fig2.get_facecolor(), edgecolor='none')
    plt.close(fig2)
    print("   [OK] 函数调用流程图已保存")
    
    print("\n" + "=" * 80)
    print(f"所有架构图已成功生成！输出目录: {output_dir}")
    print("=" * 80)


if __name__ == '__main__':
    main()