package jiangxiaopeng.ai.diet.application.service;

import jiangxiaopeng.ai.diet.application.dto.*;
import jiangxiaopeng.ai.diet.domain.model.DietRecord;
import jiangxiaopeng.ai.diet.domain.repository.DietRecordRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AiDietAnalysisService {

    private final DietRecordRepository dietRecordRepository;
    private final ChatClient chatClient;

    public AiDietAnalysisService(DietRecordRepository dietRecordRepository,
                                  @Qualifier("mainChatClient") ChatClient chatClient) {
        this.dietRecordRepository = dietRecordRepository;
        this.chatClient = chatClient;
    }

    public AiDietAnalysisDto analyze(Long petId, LocalDate date) {
        List<DietRecord> records = dietRecordRepository.findByPetIdAndDate(petId, date);

        if (records.isEmpty()) {
            return new AiDietAnalysisDto(
                    date,
                    "今日还没有饮食记录，无法进行营养分析。",
                    List.of("请先添加饮食记录"),
                    null
            );
        }

        int totalKcal = records.stream()
                .mapToInt(r -> r.getEstimatedKcal() != null ? r.getEstimatedKcal() : 0)
                .sum();

        BigDecimal totalProtein = records.stream()
                .map(r -> r.getProteinG() != null ? r.getProteinG() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalFat = records.stream()
                .map(r -> r.getFatG() != null ? r.getFatG() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCarb = records.stream()
                .map(r -> r.getCarbG() != null ? r.getCarbG() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder foodList = new StringBuilder();
        for (DietRecord record : records) {
            foodList.append(String.format("- %s: %dg, %d kcal\n",
                    record.getFoodName(),
                    record.getWeight(),
                    record.getEstimatedKcal() != null ? record.getEstimatedKcal() : 0));
        }

        String prompt = buildPrompt(date, foodList.toString(), totalKcal, totalProtein, totalFat, totalCarb);

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            return parseAiResponse(date, response, totalKcal, totalProtein, totalFat, totalCarb);
        } catch (Exception e) {
            return buildDefaultAnalysis(date, totalKcal, totalProtein, totalFat, totalCarb);
        }
    }

    private String buildPrompt(LocalDate date, String foodList, int totalKcal,
                                BigDecimal protein, BigDecimal fat, BigDecimal carb) {
        return String.format("""
                你是一位专业的宠物营养师。请根据以下宠物今日的饮食记录进行营养分析：
                
                日期：%s
                
                饮食记录：
                %s
                
                营养摄入汇总：
                - 总热量：%d kcal
                - 蛋白质：%.1f g
                - 脂肪：%.1f g
                - 碳水化合物：%.1f g
                
                请提供：
                1. 简短的营养摄入评价（1-2句话）
                2. 3条具体的改善建议
                
                注意：建议适用于一般成年犬/猫，每日目标热量约480kcal。
                """,
                date.format(DateTimeFormatter.ISO_DATE),
                foodList,
                totalKcal,
                protein.doubleValue(),
                fat.doubleValue(),
                carb.doubleValue()
        );
    }

    private AiDietAnalysisDto parseAiResponse(LocalDate date, String response,
                                               int totalKcal, BigDecimal protein,
                                               BigDecimal fat, BigDecimal carb) {
        String summary;
        List<String> suggestions = new ArrayList<>();

        String[] lines = response.split("\n");
        StringBuilder summaryBuilder = new StringBuilder();
        boolean inSuggestions = false;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("1.") || line.startsWith("2.") || line.startsWith("3.") ||
                    line.startsWith("-") || line.startsWith("•")) {
                inSuggestions = true;
                String suggestion = line.replaceFirst("^[0-9]+\\.\\s*", "")
                        .replaceFirst("^[-•]\\s*", "");
                if (!suggestion.isEmpty()) {
                    suggestions.add(suggestion);
                }
            } else if (!inSuggestions) {
                if (summaryBuilder.length() > 0) summaryBuilder.append(" ");
                summaryBuilder.append(line);
            }
        }

        summary = summaryBuilder.toString();
        if (summary.isEmpty()) {
            summary = response.length() > 200 ? response.substring(0, 200) + "..." : response;
        }

        if (suggestions.isEmpty()) {
            suggestions = getDefaultSuggestions(totalKcal);
        }

        return new AiDietAnalysisDto(
                date,
                summary,
                suggestions,
                analyzeNutrition(totalKcal, protein, fat, carb)
        );
    }

    private AiDietAnalysisDto buildDefaultAnalysis(LocalDate date, int totalKcal,
                                                    BigDecimal protein, BigDecimal fat,
                                                    BigDecimal carb) {
        String summary;
        int targetKcal = 480;

        if (totalKcal < targetKcal * 0.8) {
            summary = "今日热量摄入偏低，建议适当增加食物摄入。";
        } else if (totalKcal > targetKcal * 1.2) {
            summary = "今日热量摄入偏高，建议控制食量避免肥胖。";
        } else {
            summary = "今日热量摄入在目标范围内，继续保持！";
        }

        return new AiDietAnalysisDto(
                date,
                summary,
                getDefaultSuggestions(totalKcal),
                analyzeNutrition(totalKcal, protein, fat, carb)
        );
    }

    private List<String> getDefaultSuggestions(int totalKcal) {
        List<String> suggestions = new ArrayList<>();
        int targetKcal = 480;

        if (totalKcal < targetKcal * 0.8) {
            suggestions.add("可以适当增加主粮的分量");
            suggestions.add("添加一些鸡胸肉等高蛋白食物");
            suggestions.add("确保每日按时喂食");
        } else if (totalKcal > targetKcal * 1.2) {
            suggestions.add("减少零食的摄入");
            suggestions.add("控制主粮的分量");
            suggestions.add("增加运动量帮助消耗热量");
        } else {
            suggestions.add("继续保持均衡的饮食搭配");
            suggestions.add("可以适当添加蔬菜增加纤维摄入");
            suggestions.add("确保充足的饮水量");
        }

        return suggestions;
    }

    private AiDietAnalysisDto.NutritionAnalysisDto analyzeNutrition(int totalKcal,
                                                                     BigDecimal protein,
                                                                     BigDecimal fat,
                                                                     BigDecimal carb) {
        int targetKcal = 480;

        String calorieStatus;
        if (totalKcal < targetKcal * 0.8) {
            calorieStatus = "偏低";
        } else if (totalKcal > targetKcal * 1.2) {
            calorieStatus = "偏高";
        } else {
            calorieStatus = "正常";
        }

        double proteinRatio = protein.doubleValue() / Math.max(totalKcal, 1) * 100;
        String proteinStatus = proteinRatio > 20 ? "充足" : (proteinRatio > 15 ? "正常" : "偏低");

        double fatRatio = fat.doubleValue() * 9 / Math.max(totalKcal, 1) * 100;
        String fatStatus = fatRatio > 35 ? "偏高" : (fatRatio > 20 ? "正常" : "偏低");

        double carbRatio = carb.doubleValue() * 4 / Math.max(totalKcal, 1) * 100;
        String carbStatus = carbRatio > 50 ? "偏高" : (carbRatio > 30 ? "正常" : "正常");

        int normalCount = 0;
        if (calorieStatus.equals("正常")) normalCount++;
        if (proteinStatus.equals("正常") || proteinStatus.equals("充足")) normalCount++;
        if (fatStatus.equals("正常")) normalCount++;
        if (carbStatus.equals("正常")) normalCount++;

        String overallStatus;
        if (normalCount >= 3) {
            overallStatus = "良好";
        } else if (normalCount >= 2) {
            overallStatus = "一般";
        } else {
            overallStatus = "需改善";
        }

        return new AiDietAnalysisDto.NutritionAnalysisDto(
                proteinStatus, fatStatus, carbStatus, calorieStatus, overallStatus
        );
    }
}
