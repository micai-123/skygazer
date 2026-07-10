package com.skygazer.weather.repository;

import com.skygazer.weather.entity.VectorKnowledge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VectorKnowledgeRepository extends JpaRepository<VectorKnowledge, Long> {
    
    List<VectorKnowledge> findByCategory(String category);
    
    @Query(value = "SELECT * FROM vector_knowledge WHERE category = :category LIMIT :limit", nativeQuery = true)
    List<VectorKnowledge> findByCategoryWithLimit(@Param("category") String category, @Param("limit") int limit);
}
