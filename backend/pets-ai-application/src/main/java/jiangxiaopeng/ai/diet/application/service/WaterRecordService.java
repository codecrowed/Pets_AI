package jiangxiaopeng.ai.diet.application.service;

import jiangxiaopeng.ai.diet.application.dto.*;
import jiangxiaopeng.ai.diet.domain.model.WaterRecord;
import jiangxiaopeng.ai.diet.domain.repository.WaterRecordRepository;
import jiangxiaopeng.ai.shared.exception.BusinessException;
import jiangxiaopeng.ai.shared.exception.ErrorCode;
import jiangxiaopeng.ai.shared.utils.TimeUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class WaterRecordService {

    private static final int DEFAULT_TARGET_WATER_ML = 400;

    private final WaterRecordRepository waterRecordRepository;

    public WaterRecordService(WaterRecordRepository waterRecordRepository) {
        this.waterRecordRepository = waterRecordRepository;
    }

    public WaterRecordDto addRecord(WaterRecordAddRequest request) {
        Instant recordTime = safeParseRecordTime(request.recordTime());

        WaterRecord record = WaterRecord.create(
                request.petId(),
                request.waterAmount(),
                recordTime
        );

        record = waterRecordRepository.save(record);
        return toDto(record);
    }

    public WaterRecordDto updateRecord(Long recordId, WaterRecordUpdateRequest request) {
        WaterRecord record = waterRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "饮水记录不存在"));

        Instant recordTime = request.recordTime() != null ? safeParseRecordTime(request.recordTime()) : null;
        record.update(request.waterAmount(), recordTime);

        record = waterRecordRepository.save(record);
        return toDto(record);
    }

    public void deleteRecord(Long recordId) {
        waterRecordRepository.deleteById(recordId);
    }

    @Transactional(readOnly = true)
    public DailyWaterStatsDto getDailyWaterStats(Long petId, LocalDate date) {
        List<WaterRecord> records = waterRecordRepository.findByPetIdAndDate(petId, date);
        int totalWaterMl = waterRecordRepository.sumWaterAmountByPetIdAndDate(petId, date);
        int targetWaterMl = DEFAULT_TARGET_WATER_ML;
        int progressPercent = Math.min(100, Math.round((float) totalWaterMl / targetWaterMl * 100));

        List<WaterRecordDto> recordDtos = records.stream()
                .map(this::toDto)
                .toList();

        return new DailyWaterStatsDto(date, totalWaterMl, targetWaterMl, progressPercent, recordDtos);
    }

    private WaterRecordDto toDto(WaterRecord record) {
        return new WaterRecordDto(
                record.getId(),
                record.getPetId(),
                record.getWaterAmount(),
                record.getRecordTime(),
                record.getCreatedAt()
        );
    }

    private Instant safeParseRecordTime(String dateTimeStr) {
        try {
            return TimeUtils.parseDateTime(dateTimeStr);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, e.getMessage());
        }
    }
}
