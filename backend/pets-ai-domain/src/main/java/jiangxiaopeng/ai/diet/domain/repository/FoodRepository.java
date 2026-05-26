package jiangxiaopeng.ai.diet.domain.repository;

import jiangxiaopeng.ai.diet.domain.model.Food;

import java.util.List;
import java.util.Optional;

public interface FoodRepository {

    Optional<Food> findById(Long id);

    List<Food> searchByKeyword(String keyword, int limit, int offset);

    List<Food> findFrequentByUserId(Long userId, int limit);

    void incrementUserFoodUsage(Long userId, Long foodId);

    int countByKeyword(String keyword);
}
