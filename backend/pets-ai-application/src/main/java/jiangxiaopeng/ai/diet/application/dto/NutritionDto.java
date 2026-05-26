package jiangxiaopeng.ai.diet.application.dto;

import java.math.BigDecimal;

public record NutritionDto(
    // 基础核心指标
    Integer kcal,
    BigDecimal proteinG,
    BigDecimal fatG,
    BigDecimal carbG,
    
    // 微量元素
    BigDecimal calciumMg,
    BigDecimal phosphorusMg,
    BigDecimal sodiumMg,
    BigDecimal ironMg,
    BigDecimal zincMg,
    
    // 关键营养指标（主粮专用）
    BigDecimal crudeProteinPct,
    BigDecimal crudeFatPct,
    BigDecimal crudeAshPct,
    BigDecimal crudeFiberPct,
    
    // 其他营养成分
    BigDecimal moisturePct,
    BigDecimal taurineMg,
    BigDecimal omega3Pct,
    BigDecimal omega6Pct
) {}
