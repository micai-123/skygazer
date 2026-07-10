#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
SkyGazer架构图优化脚本 V3
- 修复字体与框架重叠问题
- 放大字体提升可读性
"""

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch
import os

plt.rcParams['font.sans-serif'] = ['Microsoft YaHei', 'SimHei', 'Arial Unicode MS']
plt.rcParams['axes.unicode_minus'] = False


def create_optimized_intelligent_decision_flow():
    fig, ax = plt.subplots(1, 1, figsize=(26, 36))
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 140)
    ax.axis('off')
    fig.patch.set_facecolor('#FAFAFA')
    
    title_box = FancyBboxPatch((5, 134), 90, 4.5, boxstyle="round,pad=0.3",
                                facecolor='#C62828', edgecolor='none', alpha=0.95)
    ax.add_patch(title_box)
    ax.text(50, 136.2, 'SkyGazer 智能决策流程图（RAG + 风险评估）', 
            ha='center', va='center', fontsize=32, fontweight='bold', color='white')
    
    layer1_box = FancyBboxPatch((3, 95), 30, 37, boxstyle="round,pad=0.5",
                                 facecolor='#FCE4EC', edgecolor='#C62828', linewidth=3)
    ax.add_patch(layer1_box)
    ax.text(18, 130, '第一层：数据输入', ha='center', va='center', 
            fontsize=22, fontweight='bold', color='#B71C1C')
    
    i1_box = FancyBboxPatch((5, 112), 26, 16, boxstyle="round,pad=0.35",
                              facecolor='white', edgecolor='#E91E63', linewidth=2)
    ax.add_patch(i1_box)
    ax.text(18, 126, '用户画像', ha='center', va='center', fontsize=18, fontweight='bold', color='#AD1457')
    profile_data = [('年龄:', '65岁'), ('健康状况:', '哮喘患者'), ('活动偏好:', '晨练跑步'), ('敏感因素:', '寒冷/花粉')]
    for i, (label, value) in enumerate(profile_data):
        ax.text(8, 122 - i*2.5, label, fontsize=15, color='#333')
        ax.text(16, 122 - i*2.5, value, fontsize=15, fontweight='bold', color='#C62828')
    
    i2_box = FancyBboxPatch((5, 102), 26, 9, boxstyle="round,pad=0.25",
                              facecolor='#FFF9C4', edgecolor='#F9A825', linewidth=2)
    ax.add_patch(i2_box)
    ax.text(18, 108.5, '用户问题', ha='center', va='center', fontsize=18, fontweight='bold', color='#F57F17')
    ax.text(18, 104.5, '"明天适合户外运动吗？"', ha='center', va='center', fontsize=16, color='#333', style='italic')
    
    ax.annotate('', xy=(18, 111), xytext=(18, 112),
                arrowprops=dict(arrowstyle='->', color='#E91E63', lw=2.5))
    
    i3_box = FancyBboxPatch((5, 86), 26, 14.5, boxstyle="round,pad=0.3",
                              facecolor='#E3F2FD', edgecolor='#1565C0', linewidth=2)
    ax.add_patch(i3_box)
    ax.text(18, 98.5, '实时天气', ha='center', va='center', fontsize=18, fontweight='bold', color='#0D47A1')
    weather_data = [('气温:', '15°C'), ('降水概率:', '80%'), ('空气质量:', 'AQI 120'), ('花粉浓度:', '高')]
    for i, (label, value) in enumerate(weather_data):
        ax.text(8, 94.5 - i*2.2, label, fontsize=14, color='#333')
        ax.text(17, 94.5 - i*2.2, value, fontsize=14, fontweight='bold', color='#1565C0')
    
    ax.annotate('', xy=(18, 85.5), xytext=(18, 101.5),
                arrowprops=dict(arrowstyle='->', color='#F9A825', lw=2.5))
    
    i4_box = FancyBboxPatch((5, 96), 26, 5.5, boxstyle="round,pad=0.25",
                              facecolor='#E8F5E9', edgecolor='#2E7D32', linewidth=2)
    ax.add_patch(i4_box)
    ax.text(18, 99, '知识库检索', ha='center', va='center', fontsize=17, fontweight='bold', color='#1B5E20')
    ax.text(18, 96.8, '哮喘患者应避免高花粉...', ha='center', va='center', fontsize=13, color='#333', style='italic')
    
    layer2_box = FancyBboxPatch((35, 62), 30, 70, boxstyle="round,pad=0.5",
                                 facecolor='#FFF9C4', edgecolor='#F9A825', linewidth=3)
    ax.add_patch(layer2_box)
    ax.text(50, 130, '第二层：智能处理', ha='center', va='center', 
            fontsize=22, fontweight='bold', color='#F57F17')
    
    p1_box = FancyBboxPatch((37, 114), 26, 14, boxstyle="round,pad=0.3",
                              facecolor='white', edgecolor='#FF9800', linewidth=2)
    ax.add_patch(p1_box)
    ax.text(50, 126, '画像解析引擎', ha='center', va='center', fontsize=17, fontweight='bold', color='#E65100')
    p1_items = ['JSON反序列化处理', '字段校验与补全', '风险因子提取', '生成结构化对象']
    for i, item in enumerate(p1_items):
        ax.text(39, 122 - i*2.2, f'• {item}', fontsize=13, color='#333')
    
    p2_box = FancyBboxPatch((37, 97), 26, 15.5, boxstyle="round,pad=0.3",
                              facecolor='#FFEBEE', edgecolor='#D32F2F', linewidth=2)
    ax.add_patch(p2_box)
    ax.text(50, 110, '风险评估算法', ha='center', va='center', fontsize=17, fontweight='bold', color='#C62828')
    ax.text(50, 107, 'RiskScore = 权重 x 因素 x 敏感系数', ha='center', va='center', fontsize=13, color='#333')
    risk_items = [('温度风险:', '4.5分'), ('空气质量风险:', '5.6分'), ('降水风险:', '2.88分'), ('总风险值:', '13.0 -> HIGH')]
    for i, (label, value) in enumerate(risk_items):
        ax.text(40, 103.5 - i*1.8, label, fontsize=13, color='#333')
        color = '#C62828' if 'HIGH' in value else '#333'
        weight = 'bold' if 'HIGH' in value else 'normal'
        ax.text(58, 103.5 - i*1.8, value, fontsize=13, color=color, fontweight=weight)
    
    p3_box = FancyBboxPatch((37, 79), 26, 16.5, boxstyle="round,pad=0.3",
                              facecolor='#E8EAF6', edgecolor='#3F51B5', linewidth=2)
    ax.add_patch(p3_box)
    ax.text(50, 93.5, 'Prompt动态组装', ha='center', va='center', fontsize=17, fontweight='bold', color='#303F9F')
    prompt_items = ['【系统角色】专业气象健康顾问', '【用户特征】65岁 + 哮喘 + 晨练', '【环境数据】15C + 高污染 + 大雨', '【知识支撑】医学建议 + 安全指南']
    for i, item in enumerate(prompt_items):
        ax.text(39, 89.5 - i*2.3, item, fontsize=12, color='#333')
    
    p4_box = FancyBboxPatch((37, 63), 26, 14.5, boxstyle="round,pad=0.3",
                              facecolor='#E0F2F1', edgecolor='#00796B', linewidth=2)
    ax.add_patch(p4_box)
    ax.text(50, 75.5, 'AI模型推理', ha='center', va='center', fontsize=17, fontweight='bold', color='#00695C')
    ai_items = ['通义千问Plus 处理', '多维信息融合分析', '个性化建议生成', '结构化JSON输出']
    for i, item in enumerate(ai_items):
        ax.text(39, 72 - i*2.2, f'• {item}', fontsize=13, color='#333')
    
    ax.annotate('', xy=(50, 113.5), xytext=(50, 114),
                arrowprops=dict(arrowstyle='->', color='#FF9800', lw=2.5))
    ax.annotate('', xy=(50, 96.5), xytext=(50, 97),
                arrowprops=dict(arrowstyle='->', color='#D32F2F', lw=2.5))
    ax.annotate('', xy=(50, 78.5), xytext=(50, 79),
                arrowprops=dict(arrowstyle='->', color='#3F51B5', lw=2.5))
    
    layer3_box = FancyBboxPatch((67, 62), 30, 70, boxstyle="round,pad=0.5",
                                 facecolor='#E0F2F1', edgecolor='#00695C', linewidth=3)
    ax.add_patch(layer3_box)
    ax.text(82, 130, '第三层：决策输出', ha='center', va='center', 
            fontsize=22, fontweight='bold', color='#004D40')
    
    o1_box = FancyBboxPatch((69, 116), 26, 12, boxstyle="round,pad=0.3",
                              facecolor='#FFCDD2', edgecolor='#B71C1C', linewidth=2)
    ax.add_patch(o1_box)
    ax.text(82, 125.5, '风险等级判定', ha='center', va='center', fontsize=17, fontweight='bold', color='#B71C1C')
    ax.text(82, 121.5, 'HIGH RISK', ha='center', va='center', fontsize=20, fontweight='bold', color='#C62828')
    ax.text(82, 118, '综合评分超过阈值10分', ha='center', va='center', fontsize=13, color='#333')
    
    o2_box = FancyBboxPatch((69, 100), 26, 14.5, boxstyle="round,pad=0.3",
                              facecolor='#FFF9C4', edgecolor='#F57F17', linewidth=2)
    ax.add_patch(o2_box)
    ax.text(82, 112.5, '核心决策建议', ha='center', va='center', fontsize=17, fontweight='bold', color='#E65100')
    advice_text = '考虑到您65岁的年龄和哮喘病史,\n结合当前高花粉浓度和轻度污染,\n强烈建议取消明天的户外晨练计划。'
    ax.text(82, 106.5, advice_text, ha='center', va='center', fontsize=13, color='#333', linespacing=1.5, style='italic')
    
    o3_box = FancyBboxPatch((69, 84), 26, 14.5, boxstyle="round,pad=0.3",
                              facecolor='#E8F5E9', edgecolor='#388E3C', linewidth=2)
    ax.add_patch(o3_box)
    ax.text(82, 96.5, '替代方案推荐', ha='center', va='center', fontsize=17, fontweight='bold', color='#2E7D32')
    alternatives = ['方案一: 室内太极拳 (20分钟)', '方案二: 下午3点后室内快走', '方案三: 使用空气净化器后拉伸']
    for i, alt in enumerate(alternatives):
        ax.text(71, 92 - i*2.3, f'[OK] {alt}', fontsize=13, color='#2E7D32')
    
    o4_box = FancyBboxPatch((69, 64), 26, 18.5, boxstyle="round,pad=0.3",
                              facecolor='#E3F2FD', edgecolor='#1976D2', linewidth=2)
    ax.add_patch(o4_box)
    ax.text(82, 80.5, '数据溯源与缓存', ha='center', va='center', fontsize=17, fontweight='bold', color='#0D47A1')
    trace_data = [('知识来源ID:', 'kb_20260411_001'), ('天气数据时间:', '2026-04-11 14:30'), ('Redis缓存TTL:', '24小时'), ('响应耗时:', '1.8秒')]
    for i, (label, value) in enumerate(trace_data):
        ax.text(71, 76 - i*2.8, label, fontsize=13, color='#333')
        ax.text(85, 76 - i*2.8, value, fontsize=13, fontweight='bold', color='#1565C0')
    
    ax.annotate('', xy=(82, 115.5), xytext=(82, 116),
                arrowprops=dict(arrowstyle='->', color='#B71C1C', lw=2.5))
    ax.annotate('', xy=(82, 99.5), xytext=(82, 100),
                arrowprops=dict(arrowstyle='->', color='#F57F17', lw=2.5))
    ax.annotate('', xy=(82, 83.5), xytext=(82, 84),
                arrowprops=dict(arrowstyle='->', color='#388E3C', lw=2.5))
    
    ax.annotate('', xy=(36, 120), xytext=(33, 120),
                arrowprops=dict(arrowstyle='->', color='#666', lw=3.5, connectionstyle="arc3,rad=0"))
    ax.annotate('', xy=(67, 120), xytext=(64, 120),
                arrowprops=dict(arrowstyle='->', color='#666', lw=3.5, connectionstyle="arc3,rad=0"))
    
    bottom_box = FancyBboxPatch((5, 55), 90, 5.5, boxstyle="round,pad=0.25",
                                 facecolor='#ECEFF1', edgecolor='#607D8B', linewidth=1.5)
    ax.add_patch(bottom_box)
    ax.text(50, 57.8, '核心价值: 基于用户画像 + 实时气象 + 医学知识库的多维智能决策系统', 
            ha='center', va='center', fontsize=17, color='#37474F', fontweight='bold')
    
    plt.tight_layout()
    return fig


def main():
    output_dir = r'd:\jishe\diagrams2'
    os.makedirs(output_dir, exist_ok=True)
    
    print("=" * 80)
    print("SkyGazer 架构图优化脚本 V3")
    print("=" * 80)
    
    print("\n[1/2] 正在生成优化的智能决策流程图...")
    fig1 = create_optimized_intelligent_decision_flow()
    fig1.savefig(os.path.join(output_dir, '05_智能决策流程图.png'), dpi=250, bbox_inches='tight',
                 facecolor=fig1.get_facecolor(), edgecolor='none')
    fig1.savefig(os.path.join(output_dir, '05_智能决策流程图.pdf'), format='pdf', bbox_inches='tight',
                 facecolor=fig1.get_facecolor(), edgecolor='none')
    plt.close(fig1)
    print("   [OK] 05_智能决策流程图 已保存")
    
    print("\n" + "=" * 80)
    print(f"优化完成！")
    print("=" * 80)


if __name__ == '__main__':
    main()