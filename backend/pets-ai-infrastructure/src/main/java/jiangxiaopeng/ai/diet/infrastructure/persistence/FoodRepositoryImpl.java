package jiangxiaopeng.ai.diet.infrastructure.persistence;

import jiangxiaopeng.ai.diet.domain.model.Food;
import jiangxiaopeng.ai.diet.domain.model.FoodCategory;
import jiangxiaopeng.ai.diet.domain.repository.FoodRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FoodRepositoryImpl implements FoodRepository {

    private final FoodMapper mapper;

    public FoodRepositoryImpl(FoodMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<Food> findById(Long id) {
        return mapper.findById(id).map(this::toDomain);
    }

    @Override
    public List<Food> searchByKeyword(String keyword, int limit, int offset) {
        return mapper.searchByKeyword(keyword, limit, offset).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Food> findFrequentByUserId(Long userId, int limit) {
        return mapper.findFrequentByUserId(userId, limit).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void incrementUserFoodUsage(Long userId, Long foodId) {
        mapper.incrementUserFoodUsage(userId, foodId);
    }

    @Override
    public int countByKeyword(String keyword) {
        return mapper.countByKeyword(keyword);
    }

    private Food toDomain(FoodEntity entity) {
        Food food = new Food();
        food.setId(entity.getId());
        food.setName(entity.getName());
        food.setIcon(entity.getIcon());
        food.setCategory(entity.getCategory() != null ? FoodCategory.valueOf(entity.getCategory()) : null);
        food.setStapleFood(Boolean.TRUE.equals(entity.getIsStapleFood()));
        food.setStapleFoodId(entity.getStapleFoodId());
        food.setKcalPer100g(entity.getKcalPer100g());
        food.setProteinPer100g(entity.getProteinPer100g());
        food.setFatPer100g(entity.getFatPer100g());
        food.setCarbPer100g(entity.getCarbPer100g());
        food.setCalciumMg(entity.getCalciumMg());
        food.setPhosphorusMg(entity.getPhosphorusMg());
        food.setSodiumMg(entity.getSodiumMg());
        food.setIronMg(entity.getIronMg());
        food.setZincMg(entity.getZincMg());
        food.setMoisturePct(entity.getMoisturePct());
        food.setTaurineMg(entity.getTaurineMg());
        food.setOmega3Pct(entity.getOmega3Pct());
        food.setOmega6Pct(entity.getOmega6Pct());
        food.setDescription(entity.getDescription());
        food.setCreatedAt(entity.getCreatedAt());
        food.setUpdatedAt(entity.getUpdatedAt());
        return food;
    }
}
