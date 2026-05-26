package jiangxiaopeng.ai.diet.domain.repository;

import jiangxiaopeng.ai.diet.domain.model.StapleFood;

import java.util.List;
import java.util.Optional;

public interface StapleFoodRepository {

    Optional<StapleFood> findById(Long id);

    List<StapleFood> searchByKeyword(String keyword, int limit, int offset);

    List<StapleFood> findByBrand(String brand);

    int countByKeyword(String keyword);
}
