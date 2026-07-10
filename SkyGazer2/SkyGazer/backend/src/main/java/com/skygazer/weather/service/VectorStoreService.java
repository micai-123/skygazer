package com.skygazer.weather.service;

import com.skygazer.weather.entity.VectorKnowledge;
import java.util.List;

public interface VectorStoreService {
    
    void addDocument(String content, String category, String title, String metadata);
    
    void addDocuments(List<VectorKnowledge> documents);
    
    List<VectorKnowledge> similaritySearch(String query, int k);
    
    List<VectorKnowledge> similaritySearchByCategory(String query, String category, int k);
    
    void deleteByCategory(String category);
    
    void deleteAll();
    
    long count();
    
    long countByCategory(String category);
}
