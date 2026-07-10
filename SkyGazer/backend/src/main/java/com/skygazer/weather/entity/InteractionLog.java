package com.skygazer.weather.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "interaction_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InteractionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(columnDefinition = "TEXT")
    private String question;
    
    @Column(name = "image_hash", length = 64)
    private String imageHash;
    
    @Column(columnDefinition = "TEXT")
    private String answer;
    
    @Column(name = "interaction_type", length = 50)
    private String interactionType;
    
    @Column(name = "model_used", length = 100)
    private String modelUsed;
    
    @Column(name = "response_time_ms")
    private Long responseTimeMs;
    
    @Column(name = "user_feedback")
    private Integer userFeedback;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
