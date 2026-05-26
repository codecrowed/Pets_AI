package jiangxiaopeng.ai.diet.infrastructure.persistence;

import jiangxiaopeng.ai.diet.domain.model.DietRecord;
import jiangxiaopeng.ai.diet.domain.model.MealType;
import jiangxiaopeng.ai.diet.domain.repository.DietRecordRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class DietRecordRepositoryImpl implements DietRecordRepository {

    private final DietRecordMapper mapper;

    public DietRecordRepositoryImpl(DietRecordMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public DietRecord save(DietRecord dietRecord) {
        DietRecordEntity entity = toEntity(dietRecord);
        mapper.save(entity);
        dietRecord.setId(entity.getId());
        return dietRecord;
    }

    @Override
    public Optional<DietRecord> findById(Long id) {
        return mapper.findById(id).map(this::toDomain);
    }

    @Override
    public List<DietRecord> findByPetIdAndDate(Long petId, LocalDate date) {
        return mapper.findByPetIdAndDate(petId, date).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<DietRecord> findByPetIdAndDateRange(Long petId, LocalDate startDate, LocalDate endDate) {
        return mapper.findByPetIdAndDateRange(petId, startDate, endDate).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        mapper.softDeleteById(id);
    }

    @Override
    public int countDistinctDatesBeforeDate(Long petId, LocalDate date) {
        return mapper.countDistinctDatesBeforeDate(petId, date);
    }

    private DietRecordEntity toEntity(DietRecord record) {
        DietRecordEntity entity = new DietRecordEntity();
        entity.setId(record.getId());
        entity.setPetId(record.getPetId());
        entity.setFoodId(record.getFoodId());
        entity.setFoodName(record.getFoodName());
        entity.setWeight(record.getWeight());
        entity.setMealType(record.getMealType() != null ? record.getMealType().name() : null);
        entity.setMealTime(record.getMealTime());
        entity.setEstimatedKcal(record.getEstimatedKcal());
        entity.setProteinG(record.getProteinG());
        entity.setFatG(record.getFatG());
        entity.setCarbG(record.getCarbG());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setDeletedAt(record.getDeletedAt());
        return entity;
    }

    private DietRecord toDomain(DietRecordEntity entity) {
        DietRecord record = new DietRecord();
        record.setId(entity.getId());
        record.setPetId(entity.getPetId());
        record.setFoodId(entity.getFoodId());
        record.setFoodName(entity.getFoodName());
        record.setWeight(entity.getWeight());
        record.setMealType(entity.getMealType() != null ? MealType.valueOf(entity.getMealType()) : null);
        record.setMealTime(entity.getMealTime());
        record.setEstimatedKcal(entity.getEstimatedKcal());
        record.setProteinG(entity.getProteinG());
        record.setFatG(entity.getFatG());
        record.setCarbG(entity.getCarbG());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setDeletedAt(entity.getDeletedAt());
        return record;
    }
}
