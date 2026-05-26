package jiangxiaopeng.ai.diet.application.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyWaterStatsDto(
    LocalDate date,
    Integer totalWaterMl,
    Integer targetWaterMl,
    Integer progressPercent,
    List<WaterRecordDto> records
) {}
