package com.skygazer.weather.service.impl;

import com.skygazer.weather.config.WeatherImageProperties;
import com.skygazer.weather.dto.response.WeatherImagePredictResponse;
import com.skygazer.weather.exception.BusinessException;
import com.skygazer.weather.exception.ErrorCode;
import com.skygazer.weather.service.WeatherImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 天气图片识别服务实现：校验图片 → 转发 Python 模型 API → 解析并映射中文结果。
 */
@Service
@Slf4j
public class WeatherImageServiceImpl implements WeatherImageService {

    private static final Map<String, String> LABEL_CN = Map.of(
            "sunny", "晴天",
            "cloudy", "多云",
            "rainy", "雨天",
            "snowy", "雪天"
    );

    private final WeatherImageProperties props;
    private final WebClient weatherImageWebClient;

    public WeatherImageServiceImpl(WeatherImageProperties props,
                                   @Qualifier("weatherImageWebClient") WebClient weatherImageWebClient) {
        this.props = props;
        this.weatherImageWebClient = weatherImageWebClient;
    }

    @Override
    public WeatherImagePredictResponse predict(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BusinessException(ErrorCode.IMAGE_FORMAT_ERROR, "上传文件为空");
        }
        byte[] bytes;
        try {
            bytes = image.getBytes();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.IMAGE_FORMAT_ERROR, "读取图片失败：" + e.getMessage());
        }
        validateBytes(bytes, image.getOriginalFilename());
        ByteArrayResource resource = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return image.getOriginalFilename();
            }
        };
        return callModel(resource);
    }

    @Override
    public WeatherImagePredictResponse predict(byte[] imageBytes, String filename) {
        validateBytes(imageBytes, filename);
        ByteArrayResource resource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
        return callModel(resource);
    }

    private WeatherImagePredictResponse callModel(ByteArrayResource resource) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("image", resource);

        Map<?, ?> raw;
        try {
            raw = weatherImageWebClient.post()
                    .uri("/predict")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                            .map(err -> {
                                log.warn("天气图片模型返回错误: {}", err);
                                return new BusinessException(ErrorCode.MODEL_INFERENCE_ERROR,
                                        "模型服务返回错误：" + err);
                            }))
                    .bodyToMono(Map.class)
                    .blockOptional()
                    .orElseThrow(() -> new BusinessException(ErrorCode.MODEL_INFERENCE_ERROR,
                            "模型服务未返回任何结果"));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用天气图片模型失败", e);
            throw new BusinessException(ErrorCode.MODEL_INFERENCE_ERROR,
                    "模型推理失败：" + e.getMessage());
        }

        String labelEn = String.valueOf(raw.get("label"));
        Object confObj = raw.get("confidence");
        double confidence = confObj == null ? 0d : Double.parseDouble(String.valueOf(confObj));

        Map<String, Double> probabilities = new LinkedHashMap<>();
        Object probObj = raw.get("probabilities");
        if (probObj instanceof Map) {
            ((Map<?, ?>) probObj).forEach((k, v) ->
                    probabilities.put(String.valueOf(k), Double.parseDouble(String.valueOf(v))));
        }

        String labelCn = LABEL_CN.getOrDefault(labelEn, labelEn);

        log.info("天气图片识别完成: {} ({}), 置信度={}", labelCn, labelEn, confidence);
        return WeatherImagePredictResponse.builder()
                .labelEn(labelEn)
                .labelCn(labelCn)
                .confidence(confidence)
                .probabilities(probabilities)
                .build();
    }

    private void validateBytes(byte[] bytes, String filename) {
        if (bytes == null || bytes.length == 0) {
            throw new BusinessException(ErrorCode.IMAGE_FORMAT_ERROR, "上传文件为空");
        }
        if (bytes.length > props.getMaxFileSize()) {
            throw new BusinessException(ErrorCode.IMAGE_TOO_LARGE,
                    "图片过大（最大 " + (props.getMaxFileSize() / 1024 / 1024) + "MB）");
        }
        if (filename != null) {
            String lower = filename.toLowerCase();
            int dot = lower.lastIndexOf('.');
            String ext = dot >= 0 ? lower.substring(dot) : "";
            if (!ext.isEmpty() && !props.getAllowedExtensions().contains(ext)) {
                throw new BusinessException(ErrorCode.IMAGE_FORMAT_ERROR,
                        "不支持的图片格式：" + ext);
            }
        }
    }
}
