package jiangxiaopeng.ai.diet.application.service;

import jiangxiaopeng.ai.diet.application.dto.*;
import jiangxiaopeng.ai.diet.domain.model.DietRecord;
import jiangxiaopeng.ai.diet.domain.model.MealType;
import jiangxiaopeng.ai.diet.domain.repository.DietRecordRepository;
import jiangxiaopeng.ai.diet.domain.repository.FoodRepository;
import jiangxiaopeng.ai.diet.domain.repository.WaterRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class DietStatsService {

    private static final int DEFAULT_TARGET_KCAL = 480;
    private static final Map<MealType, String> MEAL_ICONS = Map.of(
            MealType.BREAKFAST, "🌅",
            MealType.LUNCH, "☀️",
            MealType.DINNER, "🌙",
            MealType.SNACK, "🍬",
            MealType.SUPPLEMENT, "💊"
    );

    private final DietRecordRepository dietRecordRepository;
    private final WaterRecordRepository waterRecordRepository;
    private final FoodRepository foodRepository;

    public DietStatsService(DietRecordRepository dietRecordRepository,
                            WaterRecordRepository waterRecordRepository,
                            FoodRepository foodRepository) {
        this.dietRecordRepository = dietRecordRepository;
        this.waterRecordRepository = waterRecordRepository;
        this.foodRepository = foodRepository;
    }

    public DailyDietStatsDto getDailyStats(Long petId, LocalDate date) {
        List<DietRecord> records = dietRecordRepository.findByPetIdAndDate(petId, date);

        int totalKcal = records.stream()
                .mapToInt(r -> r.getEstimatedKcal() != null ? r.getEstimatedKcal() : 0)
                .sum();

        BigDecimal proteinG = records.stream()
                .map(r -> r.getProteinG() != null ? r.getProteinG() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal fatG = records.stream()
                .map(r -> r.getFatG() != null ? r.getFatG() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal carbG = records.stream()
                .map(r -> r.getCarbG() != null ? r.getCarbG() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int waterMl = waterRecordRepository.sumWaterAmountByPetIdAndDate(petId, date);
        int targetKcal = DEFAULT_TARGET_KCAL;
        int progressPercent = Math.min(100, Math.round((float) totalKcal / targetKcal * 100));

        List<MealGroupDto> mealGroups = groupByMealType(records);

        return new DailyDietStatsDto(
                date, totalKcal, proteinG, fatG, carbG,
                targetKcal, waterMl, progressPercent, mealGroups
        );
    }

    public WeeklyCaloriesDto getWeeklyCalories(Long petId, LocalDate endDate) {
        LocalDate startDate = endDate.minusDays(6);
        List<DietRecord> records = dietRecordRepository.findByPetIdAndDateRange(petId, startDate, endDate);

        Map<LocalDate, Integer> caloriesByDate = new LinkedHashMap<>();
        for (int i = 0; i <= 6; i++) {
            caloriesByDate.put(startDate.plusDays(i), 0);
        }

        for (DietRecord record : records) {
            LocalDate date = record.getMealDate();
            int kcal = record.getEstimatedKcal() != null ? record.getEstimatedKcal() : 0;
            caloriesByDate.merge(date, kcal, Integer::sum);
        }

        LocalDate today = LocalDate.now();
        List<DailyCaloriesDto> dailyCalories = caloriesByDate.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    String dayLabel;
                    if (date.equals(today)) {
                        dayLabel = "今天";
                    } else {
                        DayOfWeek dayOfWeek = date.getDayOfWeek();
                        dayLabel = "周" + dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE)
                                .replace("星期", "");
                    }
                    return new DailyCaloriesDto(date, dayLabel, entry.getValue(), DEFAULT_TARGET_KCAL);
                })
                .toList();

        int avgCalories = (int) dailyCalories.stream()
                .mapToInt(DailyCaloriesDto::calories)
                .average()
                .orElse(0);

        return new WeeklyCaloriesDto(dailyCalories, avgCalories, DEFAULT_TARGET_KCAL);
    }

    public ContinuousDaysDto getContinuousDays(Long petId) {
        LocalDate today = LocalDate.now();
        int days = dietRecordRepository.countDistinctDatesBeforeDate(petId, today);

        String message;
        if (days == 0) {
            message = "还没有记录，快去添加第一条吧！";
        } else if (days == 1) {
            message = "第一天记录，继续保持！";
        } else if (days < 7) {
            message = "连续记录 " + days + " 天，加油！";
        } else if (days < 30) {
            message = "连续记录 " + days + " 天，太棒了！";
        } else {
            message = "连续记录 " + days + " 天，你是最棒的主人！";
        }

        return new ContinuousDaysDto(days, message);
    }

    private List<MealGroupDto> groupByMealType(List<DietRecord> records) {
        Map<MealType, List<DietRecord>> grouped = new LinkedHashMap<>();
        for (MealType type : MealType.values()) {
            grouped.put(type, new ArrayList<>());
        }

        for (DietRecord record : records) {
            if (record.getMealType() != null) {
                grouped.get(record.getMealType()).add(record);
            }
        }

        return grouped.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(entry -> {
                    MealType mealType = entry.getKey();
                    List<DietRecord> mealRecords = entry.getValue();

                    int totalKcal = mealRecords.stream()
                            .mapToInt(r -> r.getEstimatedKcal() != null ? r.getEstimatedKcal() : 0)
                            .sum();

                    List<DietRecordDto> items = mealRecords.stream()
                            .map(r -> {
                                String foodIcon = "🍽️";
                                if (r.getFoodId() != null) {
                                    var food = foodRepository.findById(r.getFoodId()).orElse(null);
                                    if (food != null && food.getIcon() != null) {
                                        foodIcon = food.getIcon();
                                    }
                                }
                                return new DietRecordDto(
                                        r.getId(), r.getPetId(), r.getFoodId(), r.getFoodName(),
                                        foodIcon, r.getWeight(), r.getMealType().name(),
                                        r.getMealType().getLabel(), r.getMealTime(),
                                        r.getEstimatedKcal(), r.getProteinG(), r.getFatG(),
                                        r.getCarbG(), r.getCreatedAt()
                                );
                            })
                            .toList();

                    return new MealGroupDto(
                            mealType.name(),
                            mealType.getLabel(),
                            MEAL_ICONS.getOrDefault(mealType, "🍽️"),
                            totalKcal,
                            items
                    );
                })
                .toList();
    }
}
