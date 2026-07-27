package com.skygazer.weather.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云百炼 MaaS（大模型即服务）配置属性。
 * 对应 application.yml 中 ai.aliyun.* 配置项。
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.aliyun")
public class AliyunAiProperties {

    /** API Key（OpenAI 兼容模式同样使用此 Key 作为 Bearer Token） */
    private String apiKey;

    /** MaaS 服务 Host，例如 ws-5m772qyaq36bfh7a.cn-beijing.maas.aliyuncs.com */
    private String host;

    /** OpenAI 兼容模式基址，例如 https://<host>/compatible-mode/v1 */
    private String openaiCompatibleUrl;

    /** DashScope 接口基址，例如 https://<host>/api/v1 */
    private String dashscopeUrl;

    /** 调用模型/服务名称，需与阿里云 MaaS 控制台部署一致 */
    private String model = "qwen-plus";

    /** 采样温度 */
    private double temperature = 0.8;

    /** 单轮最大生成 token 数 */
    private int maxTokens = 2000;

    /** 请求超时（秒） */
    private int timeoutSeconds = 60;

    /** Embedding 模型名（用于知识库向量化，Qwen text-embedding-v2/v3） */
    private String embeddingModel = "text-embedding-v2";

    /** 向量维度（text-embedding-v2=1536，v3=1024） */
    private int vectorDim = 1536;

    /** RAG 检索返回的最大文档数 */
    private int ragTopK = 5;

    /** RAG 余弦相似度阈值（低于该值不纳入上下文） */
    private double ragSimilarityThreshold = 0.55;

    /** 多轮会话记忆保留的最大消息条数（含用户与助手） */
    private int memoryMaxMessages = 20;

    /** 知识库 Markdown 文档所在路径（classpath:knowledge/） */
    private String knowledgeBasePath = "classpath:knowledge/";

    /** 是否启用向量检索（关闭后仅工具调用+记忆，无 RAG） */
    private boolean vectorStoreEnabled = true;
}
