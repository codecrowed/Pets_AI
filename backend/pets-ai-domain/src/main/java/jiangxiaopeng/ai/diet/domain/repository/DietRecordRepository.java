package jiangxiaopeng.ai.diet.domain.repository;

import jiangxiaopeng.ai.diet.domain.model.DietRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DietRecordRepository {

    DietRecord save(DietRecord dietRecord);

    Optional<DietRecord> findById(Long id);

    List<DietRecord> findByPetIdAndDate(Long petId, LocalDate date);

    List<DietRecord> findByPetIdAndDateRange(Long petId, LocalDate startDate, LocalDate endDate);

    void deleteById(Long id);

    int countDistinctDatesBeforeDate(Long petId, LocalDate date);
}
