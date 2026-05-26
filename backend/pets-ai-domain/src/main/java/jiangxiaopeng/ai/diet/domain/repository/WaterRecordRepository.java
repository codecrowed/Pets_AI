package jiangxiaopeng.ai.diet.domain.repository;

import jiangxiaopeng.ai.diet.domain.model.WaterRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WaterRecordRepository {

    WaterRecord save(WaterRecord waterRecord);

    Optional<WaterRecord> findById(Long id);

    List<WaterRecord> findByPetIdAndDate(Long petId, LocalDate date);

    int sumWaterAmountByPetIdAndDate(Long petId, LocalDate date);

    void deleteById(Long id);
}
