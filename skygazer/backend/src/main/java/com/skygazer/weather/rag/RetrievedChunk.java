package com.skygazer.weather.rag;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 向量检索返回的片段（带相似度分数）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetrievedChunk {

    private String id;
    private String title;
    private String category;
    private String content;
    private double score;
    private Map<String, Object> metadata;
}
