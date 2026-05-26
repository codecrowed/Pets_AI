package jiangxiaopeng.ai.diet.infrastructure.persistence;

import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface WaterRecordMapper {

    @Select("""
            SELECT id, pet_id, water_amount, record_time, created_at, updated_at, deleted_at
            FROM pet_water_records
            WHERE id = #{id} AND deleted_at IS NULL
            """)
    Optional<WaterRecordEntity> findById(Long id);

    @Select("""
            SELECT id, pet_id, water_amount, record_time, created_at, updated_at, deleted_at
            FROM pet_water_records
            WHERE pet_id = #{petId} 
              AND DATE(record_time) = #{date}
              AND deleted_at IS NULL
            ORDER BY record_time ASC
            """)
    List<WaterRecordEntity> findByPetIdAndDate(@Param("petId") Long petId, @Param("date") LocalDate date);

    @Select("""
            SELECT COALESCE(SUM(water_amount), 0)
            FROM pet_water_records
            WHERE pet_id = #{petId} 
              AND DATE(record_time) = #{date}
              AND deleted_at IS NULL
            """)
    int sumWaterAmountByPetIdAndDate(@Param("petId") Long petId, @Param("date") LocalDate date);

    @Insert("""
            INSERT INTO pet_water_records (pet_id, water_amount, record_time, created_at, updated_at)
            VALUES (#{petId}, #{waterAmount}, #{recordTime}, 
                    COALESCE(#{createdAt}, now()), COALESCE(#{updatedAt}, now()))
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(WaterRecordEntity entity);

    @Update("""
            UPDATE pet_water_records
            SET water_amount = #{waterAmount}, record_time = #{recordTime}, updated_at = now()
            WHERE id = #{id} AND deleted_at IS NULL
            """)
    int updateById(WaterRecordEntity entity);

    @Update("""
            UPDATE pet_water_records
            SET deleted_at = now(), updated_at = now()
            WHERE id = #{id}
            """)
    int softDeleteById(Long id);

    default WaterRecordEntity save(WaterRecordEntity entity) {
        if (entity.getId() == null) {
            insert(entity);
        } else {
            updateById(entity);
        }
        return entity;
    }
}
