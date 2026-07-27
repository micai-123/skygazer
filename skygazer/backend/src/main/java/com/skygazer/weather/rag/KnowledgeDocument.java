package com.skygazer.weather.rag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 知识库文档（与 Spring AI 的 Document 解耦，便于自研向量存储）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeDocument {

    /** 文档唯一 ID */
    private String id;

    /** 标题（用于引用展示） */
    private String title;

    /** 分类（如 气象基础 / 灾害防御 / 生活指数） */
    private String category;

    /** 正文内容 */
    private String content;

    /** 附加元数据 */
    private Map<String, Object> metadata;
}
