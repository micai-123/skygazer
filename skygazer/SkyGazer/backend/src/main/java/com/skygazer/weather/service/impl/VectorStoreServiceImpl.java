package com.skygazer.weather.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skygazer.weather.entity.VectorKnowledge;
import com.skygazer.weather.repository.VectorKnowledgeRepository;
import com.skygazer.weather.service.QwenEmbeddingService;
import com.skygazer.weather.service.VectorStoreService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreServiceImpl implements VectorStoreService {
    
    private final VectorKnowledgeRepository vectorKnowledgeRepository;
    private final QwenEmbeddingService qwenEmbeddingService;
    private final ObjectMapper objectMapper;
    
    private static final int MAX_SNIPPET_LENGTH = 200;
    
    @Override
    @Transactional
    public void addDocument(String content, String category, String title, String metadata) {
        try {
            float[] embedding = generateEmbedding(content);
            String embeddingJson = objectMapper.writeValueAsString(embedding);
            
            VectorKnowledge knowledge = VectorKnowledge.builder()
                .content(content)
                .embedding(embeddingJson)
                .category(category)
                .title(title)
                .metadata(metadata)
                .build();
            
            vectorKnowledgeRepository.save(knowledge);
            log.debug("添加文档成功: category={}, title={}", category, title);
        } catch (JsonProcessingException e) {
            log.error("序列化向量失败: {}", e.getMessage());
            throw new RuntimeException("添加文档失败", e);
        }
    }
    
    @Override
    @Transactional
    public void addDocuments(List<VectorKnowledge> documents) {
        List<VectorKnowledge> processedDocuments = documents.stream()
            .map(this::processDocument)
            .collect(Collectors.toList());
        
        vectorKnowledgeRepository.saveAll(processedDocuments);
        log.info("批量添加文档成功: count={}", documents.size());
    }
    
    private VectorKnowledge processDocument(VectorKnowledge doc) {
        try {
            float[] embedding = generateEmbedding(doc.getContent());
            String embeddingJson = objectMapper.writeValueAsString(embedding);
            doc.setEmbedding(embeddingJson);
            return doc;
        } catch (JsonProcessingException e) {
            log.error("处理文档失败: {}", e.getMessage());
            throw new RuntimeException("处理文档失败", e);
        }
    }
    
    @Override
    public List<VectorKnowledge> similaritySearch(String query, int k) {
        float[] queryEmbedding = generateEmbedding(query);
        
        List<VectorKnowledge> allDocuments = vectorKnowledgeRepository.findAll();
        
        return allDocuments.stream()
            .map(doc -> {
                float similarity = calculateSimilarity(queryEmbedding, doc.getEmbedding());
                doc.setMetadata(doc.getMetadata() + "|similarity:" + similarity);
                return doc;
            })
            .sorted((a, b) -> {
                float simA = extractSimilarity(a.getMetadata());
                float simB = extractSimilarity(b.getMetadata());
                return Float.compare(simB, simA);
            })
            .limit(k)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<VectorKnowledge> similaritySearchByCategory(String query, String category, int k) {
        float[] queryEmbedding = generateEmbedding(query);
        
        List<VectorKnowledge> categoryDocuments = vectorKnowledgeRepository.findByCategory(category);
        
        return categoryDocuments.stream()
            .map(doc -> {
                float similarity = calculateSimilarity(queryEmbedding, doc.getEmbedding());
                doc.setMetadata(doc.getMetadata() + "|similarity:" + similarity);
                return doc;
            })
            .sorted((a, b) -> {
                float simA = extractSimilarity(a.getMetadata());
                float simB = extractSimilarity(b.getMetadata());
                return Float.compare(simB, simA);
            })
            .limit(k)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteByCategory(String category) {
        List<VectorKnowledge> documents = vectorKnowledgeRepository.findByCategory(category);
        vectorKnowledgeRepository.deleteAll(documents);
        log.info("删除分类文档成功: category={}, count={}", category, documents.size());
    }
    
    @Override
    @Transactional
    public void deleteAll() {
        vectorKnowledgeRepository.deleteAll();
        log.info("删除所有文档成功");
    }
    
    @Override
    public long count() {
        return vectorKnowledgeRepository.count();
    }
    
    @Override
    public long countByCategory(String category) {
        return vectorKnowledgeRepository.findByCategory(category).size();
    }
    
    private float[] generateEmbedding(String text) {
        try {
            List<Double> embeddingList = qwenEmbeddingService.embed(text);
            if (embeddingList != null && !embeddingList.isEmpty()) {
                float[] embedding = new float[embeddingList.size()];
                for (int i = 0; i < embeddingList.size(); i++) {
                    embedding[i] = embeddingList.get(i).floatValue();
                }
                return embedding;
            }
            log.warn("Embedding服务返回空结果，使用简化方法");
            return generateSimpleEmbedding(text);
        } catch (Exception e) {
            log.warn("生成嵌入向量失败，使用简化方法: {}", e.getMessage());
            return generateSimpleEmbedding(text);
        }
    }
    
    private float[] generateSimpleEmbedding(String text) {
        float[] embedding = new float[1536];
        String[] words = text.toLowerCase().split("\\s+");
        for (int i = 0; i < words.length && i < embedding.length; i++) {
            embedding[i] = (float) words[i].hashCode() / Integer.MAX_VALUE;
        }
        return embedding;
    }
    
    private float calculateSimilarity(float[] queryEmbedding, String storedEmbeddingJson) {
        if (storedEmbeddingJson == null || storedEmbeddingJson.isEmpty()) {
            return 0.0f;
        }
        try {
            float[] storedEmbedding = objectMapper.readValue(storedEmbeddingJson, float[].class);
            return cosineSimilarity(queryEmbedding, storedEmbedding);
        } catch (JsonProcessingException e) {
            log.error("解析向量失败: {}", e.getMessage());
            return 0.0f;
        }
    }
    
    private float cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) {
            return 0.0f;
        }
        
        float dotProduct = 0.0f;
        float normA = 0.0f;
        float normB = 0.0f;
        
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        
        if (normA == 0 || normB == 0) {
            return 0.0f;
        }
        
        return dotProduct / (float) (Math.sqrt(normA) * Math.sqrt(normB));
    }
    
    private float extractSimilarity(String metadata) {
        if (metadata == null || !metadata.contains("similarity:")) {
            return 0.0f;
        }
        try {
            String[] parts = metadata.split("\\|");
            for (String part : parts) {
                if (part.startsWith("similarity:")) {
                    return Float.parseFloat(part.substring("similarity:".length()));
                }
            }
        } catch (NumberFormatException e) {
            log.warn("解析相似度失败: {}", e.getMessage());
        }
        return 0.0f;
    }
}
