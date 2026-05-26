package jiangxiaopeng.ai.diet.infrastructure.persistence;

import jiangxiaopeng.ai.diet.domain.model.StapleFood;
import jiangxiaopeng.ai.diet.domain.model.StapleFoodType;
import jiangxiaopeng.ai.diet.domain.model.TargetAge;
import jiangxiaopeng.ai.diet.domain.model.TargetSize;
import jiangxiaopeng.ai.diet.domain.model.TargetSpecies;
import jiangxiaopeng.ai.diet.domain.repository.StapleFoodRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class StapleFoodRepositoryImpl implements StapleFoodRepository {

    private final StapleFoodMapper mapper;

    public StapleFoodRepositoryImpl(StapleFoodMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<StapleFood> findById(Long id) {
        return mapper.findById(id).map(this::toDomain);
    }

    @Override
    public List<StapleFood> searchByKeyword(String keyword, int limit, int offset) {
        return mapper.searchByKeyword(keyword, limit, offset).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<StapleFood> findByBrand(String brand) {
        return mapper.findByBrand(brand).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public int countByKeyword(String keyword) {
        return mapper.countByKeyword(keyword);
    }

    private StapleFood toDomain(StapleFoodEntity entity) {
        StapleFood stapleFood = new StapleFood();
        stapleFood.setId(entity.getId());
        stapleFood.setName(entity.getName());
        stapleFood.setBrand(entity.getBrand());
        stapleFood.setFoodType(parseEnum(StapleFoodType.class, entity.getFoodType()));
        stapleFood.setTargetSpecies(parseEnum(TargetSpecies.class, entity.getTargetSpecies()));
        stapleFood.setTargetBreed(entity.getTargetBreed());
        stapleFood.setTargetAge(parseEnum(TargetAge.class, entity.getTargetAge()));
        stapleFood.setTargetSize(parseEnum(TargetSize.class, entity.getTargetSize()));
        stapleFood.setCrudeProteinPct(entity.getCrudeProteinPct());
        stapleFood.setCrudeFatPct(entity.getCrudeFatPct());
        stapleFood.setCrudeAshPct(entity.getCrudeAshPct());
        stapleFood.setCrudeFiberPct(entity.getCrudeFiberPct());
        stapleFood.setMoisturePct(entity.getMoisturePct());
        stapleFood.setNetWeightG(entity.getNetWeightG());
        stapleFood.setPriceYuan(entity.getPriceYuan());
        stapleFood.setBarcode(entity.getBarcode());
        stapleFood.setImageUrl(entity.getImageUrl());
        stapleFood.setDescription(entity.getDescription());
        stapleFood.setIngredients(entity.getIngredients());
        stapleFood.setFeedingGuide(entity.getFeedingGuide());
        stapleFood.setCreatedAt(entity.getCreatedAt());
        stapleFood.setUpdatedAt(entity.getUpdatedAt());
        return stapleFood;
    }

    private <T extends Enum<T>> T parseEnum(Class<T> enumClass, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
