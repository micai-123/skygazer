package com.skygazer.weather.service;

import java.util.List;

public interface QwenEmbeddingService {
    List<Double> embed(String text);
}
