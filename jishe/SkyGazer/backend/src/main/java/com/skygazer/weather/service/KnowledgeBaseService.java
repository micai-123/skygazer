package com.skygazer.weather.service;

import com.skygazer.weather.entity.VectorKnowledge;
import java.util.List;

public interface KnowledgeBaseService {
    
    void initializeKnowledgeBase();
    
    void addWeatherWarningKnowledge();
    
    void addLifestyleRuleKnowledge();
    
    void addActivityAdviceKnowledge();
    
    void addWeatherPhenomenonKnowledge();
    
    List<VectorKnowledge> searchKnowledge(String query, int limit);
    
    List<VectorKnowledge> searchKnowledgeByCategory(String query, String category, int limit);
    
    void refreshKnowledgeBase();
}
