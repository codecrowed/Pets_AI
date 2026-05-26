package jiangxiaopeng.ai.diet.application.dto;

import java.time.Instant;

public record WaterRecordDto(
    Long id,
    Long petId,
    Integer waterAmount,
    Instant recordTime,
    Instant createdAt
) {}
