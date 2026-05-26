package jiangxiaopeng.ai.diet.infrastructure.persistence;

import jiangxiaopeng.ai.diet.domain.model.WaterRecord;
import jiangxiaopeng.ai.diet.domain.repository.WaterRecordRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class WaterRecordRepositoryImpl implements WaterRecordRepository {

    private final WaterRecordMapper mapper;

    public WaterRecordRepositoryImpl(WaterRecordMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public WaterRecord save(WaterRecord waterRecord) {
        WaterRecordEntity entity = toEntity(waterRecord);
        mapper.save(entity);
        waterRecord.setId(entity.getId());
        return waterRecord;
    }

    @Override
    public Optional<WaterRecord> findById(Long id) {
        return mapper.findById(id).map(this::toDomain);
    }

    @Override
    public List<WaterRecord> findByPetIdAndDate(Long petId, LocalDate date) {
        return mapper.findByPetIdAndDate(petId, date).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public int sumWaterAmountByPetIdAndDate(Long petId, LocalDate date) {
        return mapper.sumWaterAmountByPetIdAndDate(petId, date);
    }

    @Override
    public void deleteById(Long id) {
        mapper.softDeleteById(id);
    }

    private WaterRecordEntity toEntity(WaterRecord record) {
        WaterRecordEntity entity = new WaterRecordEntity();
        entity.setId(record.getId());
        entity.setPetId(record.getPetId());
        entity.setWaterAmount(record.getWaterAmount());
        entity.setRecordTime(record.getRecordTime());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setDeletedAt(record.getDeletedAt());
        return entity;
    }

    private WaterRecord toDomain(WaterRecordEntity entity) {
        WaterRecord record = new WaterRecord();
        record.setId(entity.getId());
        record.setPetId(entity.getPetId());
        record.setWaterAmount(entity.getWaterAmount());
        record.setRecordTime(entity.getRecordTime());
        record.setCreatedAt(entity.getCreatedAt());
        record.setUpdatedAt(entity.getUpdatedAt());
        record.setDeletedAt(entity.getDeletedAt());
        return record;
    }
}
