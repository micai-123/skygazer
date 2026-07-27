package com.skygazer.weather.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentQueryRequest {

    /** 用户问题 */
    private String question;

    /** 用户所在位置（可选，用于增强气象上下文） */
    private String location;

    /** 会话 ID（可选，用于多轮上下文关联） */
    private String sessionId;

    /** 补充上下文（可选） */
    private String context;
}
