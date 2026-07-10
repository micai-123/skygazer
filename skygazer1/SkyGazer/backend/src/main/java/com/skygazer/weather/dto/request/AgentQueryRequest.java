package com.skygazer.weather.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentQueryRequest {
    
    @NotBlank(message = "问题不能为空")
    private String question;
    
    private String location;
    
    private String sessionId;
    
    private String context;
    
    private String style;
}
