package jiangxiaopeng.ai.diet.application.service;

import jiangxiaopeng.ai.diet.application.dto.FoodDto;
import jiangxiaopeng.ai.diet.application.dto.StapleFoodDto;
import jiangxiaopeng.ai.diet.domain.model.Food;
import jiangxiaopeng.ai.diet.domain.model.StapleFood;
import jiangxiaopeng.ai.diet.domain.repository.FoodRepository;
import jiangxiaopeng.ai.diet.domain.repository.StapleFoodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FoodService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final FoodRepository foodRepository;
    private final StapleFoodRepository stapleFoodRepository;

    public FoodService(FoodRepository foodRepository, StapleFoodRepository stapleFoodRepository) {
        this.foodRepository = foodRepository;
        this.stapleFoodRepository = stapleFoodRepository;
    }

    public List<FoodDto> searchFoods(String keyword, Integer pageNum, Integer pageSize) {
        int limit = pageSize != null && pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        int offset = (pageNum != null && pageNum > 0 ? pageNum - 1 : 0) * limit;

        List<Food> foods = foodRepository.searchByKeyword(keyword, limit, offset);

        return foods.stream()
                .map(this::toDto)
                .toList();
    }

    public List<FoodDto> getFrequentFoods(Long userId) {
        List<Food> foods = foodRepository.findFrequentByUserId(userId, 10);
        return foods.stream()
                .map(this::toDto)
                .toList();
    }

    private FoodDto toDto(Food food) {
        StapleFoodDto stapleFoodDto = null;
        if (food.isStapleFood() && food.getStapleFoodId() != null) {
            StapleFood stapleFood = stapleFoodRepository.findById(food.getStapleFoodId()).orElse(null);
            if (stapleFood != null) {
                stapleFoodDto = toStapleFoodDto(stapleFood);
            }
        }

        return new FoodDto(
                food.getId(),
                food.getName(),
                food.getIcon(),
                food.getCategory() != null ? food.getCategory().name() : null,
                food.getCategory() != null ? food.getCategory().getLabel() : null,
                food.isStapleFood(),
                food.getStapleFoodId(),
                stapleFoodDto,
                food.getKcalPer100g(),
                food.getProteinPer100g(),
                food.getFatPer100g(),
                food.getCarbPer100g(),
                food.getDescription()
        );
    }

    private StapleFoodDto toStapleFoodDto(StapleFood sf) {
        return new StapleFoodDto(
                sf.getId(),
                sf.getName(),
                sf.getBrand(),
                sf.getFoodType() != null ? sf.getFoodType().name() : null,
                sf.getFoodType() != null ? sf.getFoodType().getLabel() : null,
                sf.getTargetSpecies() != null ? sf.getTargetSpecies().name() : null,
                sf.getTargetSpecies() != null ? sf.getTargetSpecies().getLabel() : null,
                sf.getTargetBreed(),
                sf.getTargetAge() != null ? sf.getTargetAge().name() : null,
                sf.getTargetAge() != null ? sf.getTargetAge().getLabel() : null,
                sf.getTargetSize() != null ? sf.getTargetSize().name() : null,
                sf.getTargetSize() != null ? sf.getTargetSize().getLabel() : null,
                sf.getCrudeProteinPct(),
                sf.getCrudeFatPct(),
                sf.getCrudeAshPct(),
                sf.getCrudeFiberPct(),
                sf.getMoisturePct(),
                sf.getImageUrl(),
                sf.getDescription(),
                sf.getIngredients(),
                sf.getFeedingGuide()
        );
    }
}
