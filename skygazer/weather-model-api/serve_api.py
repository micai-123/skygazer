#!/usr/bin/env python3
"""天气图像分类模型 — Flask REST API 服务

将天气分类模型（ResNet-18 + Dropout 分类头）封装为可独立运行的 HTTP 服务，
供其他项目接入。调用方上传单张天气图片，服务返回识别的天气类型、置信度及四类概率。

接口：
    GET  /health   健康检查与服务元信息
    POST /predict  接收 multipart/form-data 图片（字段名 image），返回 JSON 结果

返回结构（/predict）：
    {
        "label": "sunny",          # 预测类别
        "confidence": 0.9231,       # 最高类概率（置信度）
        "probabilities": {          # 四分类概率分布
            "cloudy": 0.01,
            "rainy": 0.02,
            "snowy": 0.05,
            "sunny": 0.92
        }
    }

模型结构、类别顺序、预处理流程均严格对齐 main.py，确保与训练时一致。
"""

import os
import io

import numpy as np
import cv2
import torch
import torch.nn as nn
from flask import Flask, request, jsonify

# ============================================================
# 配置
# ============================================================
# 模型路径：默认相对项目根目录，可通过环境变量覆盖
MODEL_PATH = os.environ.get(
    'WEATHER_MODEL_PATH',
    os.path.join(os.path.dirname(os.path.abspath(__file__)), 'results', 'model_sample.pth'),
)
HOST = os.environ.get('WEATHER_API_HOST', '0.0.0.0')
PORT = int(os.environ.get('WEATHER_API_PORT', '5000'))
# 限制请求体大小（默认 10MB），防止超大文件
MAX_CONTENT_LENGTH = int(os.environ.get('WEATHER_API_MAX_CONTENT_LENGTH', 10 * 1024 * 1024))

IM_SIZE = 224
LABEL = ['cloudy', 'rainy', 'snowy', 'sunny']

# ImageNet 标准化（与训练、main.py 完全一致）
MEAN = np.array([0.485, 0.456, 0.406], dtype=np.float32)
STD = np.array([0.229, 0.224, 0.225], dtype=np.float32)

ALLOWED_EXT = {'.jpg', '.jpeg', '.png', '.bmp', '.webp'}

device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')


# ============================================================
# 模型结构（必须与 main.py / model_sample.pth 一致）
# ============================================================
class ClassificationHead(nn.Module):
    """分类头：Dropout -> Linear，与训练/main.py 结构完全一致。"""

    def __init__(self, in_features, num_classes, dropout=0.3):
        super().__init__()
        self.layers = nn.Sequential(
            nn.Dropout(dropout),
            nn.Linear(in_features, num_classes),
        )

    def forward(self, x):
        return self.layers(x)


def _build_model():
    """创建 ResNet-18 + ClassificationHead 模型结构，兼容新旧 torchvision API。"""
    import torchvision.models as models

    try:
        model = models.resnet18(weights=None)
    except TypeError:
        model = models.resnet18(pretrained=False)

    in_features = model.fc.in_features
    model.fc = ClassificationHead(in_features, 4, dropout=0.3)
    return model


# ============================================================
# 模型加载（模块级，仅加载一次）
# ============================================================
def _load_model():
    if not os.path.exists(MODEL_PATH):
        raise FileNotFoundError(f'模型文件不存在: {MODEL_PATH}')

    model = _build_model()
    try:
        state_dict = torch.load(MODEL_PATH, map_location=device)
        if isinstance(state_dict, dict) and 'model_state_dict' in state_dict:
            state_dict = state_dict['model_state_dict']
        model.load_state_dict(state_dict)
    except Exception as e:
        raise RuntimeError(f'模型加载失败: {e}') from e

    model = model.to(device)
    model.eval()
    return model


model = _load_model()
print(f'[serve_api] 模型加载成功: {MODEL_PATH}  device={device}')


# ============================================================
# 预处理与推理（与 main.py 的 predict 完全一致）
# ============================================================
def preprocess(img_bytes: bytes) -> torch.Tensor:
    """将上传的图片字节流解码并预处理为模型输入张量 (1,3,224,224)。"""
    arr = np.frombuffer(img_bytes, dtype=np.uint8)
    img = cv2.imdecode(arr, cv2.IMREAD_COLOR)
    if img is None:
        raise ValueError('无法解码图片，请确认文件为有效的图像格式')

    # 1. BGR -> RGB
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    # 2. resize 到 224x224（与训练 transforms.Resize((224,224)) 一致）
    img = cv2.resize(img, (IM_SIZE, IM_SIZE), interpolation=cv2.INTER_LINEAR)
    # 3. 归一化到 [0,1]
    img = img.astype(np.float32) / 255.0
    # 4. ImageNet 标准化
    img = (img - MEAN) / STD
    # 5. HWC -> CHW -> NCHW
    img = np.transpose(img, (2, 0, 1))
    img = np.expand_dims(img, axis=0)
    return torch.from_numpy(img).to(device)


def predict(img_bytes: bytes) -> dict:
    """对图片字节流推理，返回结构化结果。"""
    tensor = preprocess(img_bytes)
    with torch.no_grad():
        output = model(tensor)
        probs = torch.softmax(output, dim=1)[0]
        pred_idx = int(torch.argmax(probs, dim=0).item())
        confidence = float(probs[pred_idx].item())

    probabilities = {LABEL[i]: float(probs[i].item()) for i in range(len(LABEL))}
    return {
        'label': LABEL[pred_idx],
        'confidence': confidence,
        'probabilities': probabilities,
    }


# ============================================================
# Flask 应用
# ============================================================
app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = MAX_CONTENT_LENGTH


@app.route('/health', methods=['GET'])
def health():
    return jsonify({
        'status': 'ok',
        'model_loaded': model is not None,
        'model_path': MODEL_PATH,
        'device': str(device),
        'classes': LABEL,
    })


@app.route('/predict', methods=['POST'])
def predict_route():
    # 1. 取上传文件
    if 'image' not in request.files:
        return jsonify({'error': "缺少上传字段 'image'（multipart/form-data）"}), 400

    file = request.files['image']
    if not file or file.filename == '':
        return jsonify({'error': '上传文件为空'}), 400

    # 2. 扩展名校验（辅助手段，最终以解码结果为准）
    ext = os.path.splitext(file.filename)[1].lower()
    if ext and ext not in ALLOWED_EXT:
        return jsonify({'error': f'不支持的文件类型: {ext}'}), 400

    # 3. 读取字节流
    data = file.read()
    if not data:
        return jsonify({'error': '文件内容为空'}), 400

    # 4. 推理
    try:
        result = predict(data)
    except ValueError as e:
        return jsonify({'error': str(e)}), 400
    except Exception as e:
        app.logger.exception('推理失败')
        return jsonify({'error': '推理失败，请稍后重试'}), 500

    return jsonify(result)


if __name__ == '__main__':
    app.run(host=HOST, port=PORT, debug=False)
