package com.skygazer.weather.service;

import com.skygazer.weather.dto.response.WeatherImagePredictResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 天气图片识别服务：接收前端上传的图片并转发至 Python 模型 API 进行四分类推理。
 */
public interface WeatherImageService {

    /**
     * 调用 Python 天气分类模型识别图片天气类型。
     *
     * @param image 用户上传的图片（multipart）
     * @return 识别结果（含中文映射与概率分布）
     */
    WeatherImagePredictResponse predict(MultipartFile image);

    /**
     * 以原始字节形式调用模型（用于 base64 解码后的图片）。
     *
     * @param imageBytes 图片字节
     * @param filename   文件名（含扩展名）
     * @return 识别结果（含中文映射与概率分布）
     */
    WeatherImagePredictResponse predict(byte[] imageBytes, String filename);
}
