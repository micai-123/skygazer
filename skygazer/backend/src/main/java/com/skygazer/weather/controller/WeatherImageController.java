package com.skygazer.weather.controller;

import com.skygazer.weather.dto.response.ApiResponse;
import com.skygazer.weather.dto.response.WeatherImagePredictResponse;
import com.skygazer.weather.service.WeatherImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 天气图片识别接口。接收前端上传的天气图片，调用 Python 模型进行四分类推理。
 */
@RestController
@RequestMapping("/weather-image")
@RequiredArgsConstructor
@Slf4j
public class WeatherImageController {

    private final WeatherImageService weatherImageService;

    /**
     * 上传天气图片并返回识别结果。
     *
     * @param image multipart 表单字段 "image"
     */
    @PostMapping(value = "/predict", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<WeatherImagePredictResponse> predict(
            @RequestPart("image") MultipartFile image) {
        return ApiResponse.success("识别完成", weatherImageService.predict(image));
    }
}
