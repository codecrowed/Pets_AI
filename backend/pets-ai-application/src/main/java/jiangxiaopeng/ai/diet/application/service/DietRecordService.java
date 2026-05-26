package jiangxiaopeng.ai.diet.application.service;

import jiangxiaopeng.ai.diet.application.dto.*;
import jiangxiaopeng.ai.diet.domain.model.DietRecord;
import jiangxiaopeng.ai.diet.domain.model.Food;
import jiangxiaopeng.ai.diet.domain.model.MealType;
import jiangxiaopeng.ai.diet.domain.repository.DietRecordRepository;
import jiangxiaopeng.ai.diet.domain.repository.FoodRepository;
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
public class DietRecordService {

    private final DietRecordRepository dietRecordRepository;
    private final FoodRepository foodRepository;

    public DietRecordService(DietRecordRepository dietRecordRepository, FoodRepository foodRepository) {
        this.dietRecordRepository = dietRecordRepository;
        this.foodRepository = foodRepository;
    }

    public DietRecordDto addRecord(DietRecordAddRequest request, Long userId) {
        MealType mealType = MealType.valueOf(request.mealType());
        Instant mealTime = safeParseMealTime(request.mealTime());

        DietRecord record = DietRecord.create(
                request.petId(),
                request.foodId(),
                request.foodName(),
                request.weight(),
                mealType,
                mealTime
        );

        if (request.foodId() != null) {
            Food food = foodRepository.findById(request.foodId()).orElse(null);
            if (food != null) {
                record.calculateNutrition(food);
                foodRepository.incrementUserFoodUsage(userId, request.foodId());
            }
        }

        record = dietRecordRepository.save(record);
        return toDto(record);
    }

    public DietRecordDto updateRecord(Long recordId, DietRecordUpdateRequest request) {
        DietRecord record = dietRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "饮食记录不存在"));

        MealType mealType = request.mealType() != null ? MealType.valueOf(request.mealType()) : null;
        Instant mealTime = request.mealTime() != null ? safeParseMealTime(request.mealTime()) : null;

        record.update(request.foodName(), request.weight(), mealType, mealTime);

        if (record.getFoodId() != null) {
            Food food = foodRepository.findById(record.getFoodId()).orElse(null);
            if (food != null) {
                record.calculateNutrition(food);
            }
        }

        record = dietRecordRepository.save(record);
        return toDto(record);
    }

    public void deleteRecord(Long recordId) {
        dietRecordRepository.deleteById(recordId);
    }

    @Transactional(readOnly = true)
    public List<DietRecordDto> getRecordsByDate(Long petId, LocalDate date) {
        return dietRecordRepository.findByPetIdAndDate(petId, date).stream()
                .map(this::toDto)
                .toList();
    }

    private DietRecordDto toDto(DietRecord record) {
        MealType mealType = record.getMealType();
        String foodIcon = "🍽️";
        
        if (record.getFoodId() != null) {
            Food food = foodRepository.findById(record.getFoodId()).orElse(null);
            if (food != null && food.getIcon() != null) {
                foodIcon = food.getIcon();
            }
        }

        return new DietRecordDto(
                record.getId(),
                record.getPetId(),
                record.getFoodId(),
                record.getFoodName(),
                foodIcon,
                record.getWeight(),
                mealType != null ? mealType.name() : null,
                mealType != null ? mealType.getLabel() : null,
                record.getMealTime(),
                record.getEstimatedKcal(),
                record.getProteinG(),
                record.getFatG(),
                record.getCarbG(),
                record.getCreatedAt()
        );
    }

    private Instant safeParseMealTime(String dateTimeStr) {
        try {
            return TimeUtils.parseDateTime(dateTimeStr);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, e.getMessage());
        }
    }
}
