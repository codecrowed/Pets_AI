package jiangxiaopeng.ai.diet.infrastructure.persistence;

import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface DietRecordMapper {

    @Select("""
            SELECT id, pet_id, food_id, food_name, weight, meal_type, meal_time, 
                   estimated_kcal, protein_g, fat_g, carb_g, created_at, updated_at, deleted_at
            FROM pet_diet_records
            WHERE id = #{id} AND deleted_at IS NULL
            """)
    Optional<DietRecordEntity> findById(Long id);

    @Select("""
            SELECT id, pet_id, food_id, food_name, weight, meal_type, meal_time, 
                   estimated_kcal, protein_g, fat_g, carb_g, created_at, updated_at, deleted_at
            FROM pet_diet_records
            WHERE pet_id = #{petId} 
              AND DATE(meal_time) = #{date}
              AND deleted_at IS NULL
            ORDER BY meal_time ASC
            """)
    List<DietRecordEntity> findByPetIdAndDate(@Param("petId") Long petId, @Param("date") LocalDate date);

    @Select("""
            SELECT id, pet_id, food_id, food_name, weight, meal_type, meal_time, 
                   estimated_kcal, protein_g, fat_g, carb_g, created_at, updated_at, deleted_at
            FROM pet_diet_records
            WHERE pet_id = #{petId} 
              AND DATE(meal_time) BETWEEN #{startDate} AND #{endDate}
              AND deleted_at IS NULL
            ORDER BY meal_time ASC
            """)
    List<DietRecordEntity> findByPetIdAndDateRange(@Param("petId") Long petId, 
                                                   @Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);

    @Insert("""
            INSERT INTO pet_diet_records (pet_id, food_id, food_name, weight, meal_type, meal_time, 
                                          estimated_kcal, protein_g, fat_g, carb_g, created_at, updated_at)
            VALUES (#{petId}, #{foodId}, #{foodName}, #{weight}, #{mealType}, #{mealTime},
                    #{estimatedKcal}, #{proteinG}, #{fatG}, #{carbG}, 
                    COALESCE(#{createdAt}, now()), COALESCE(#{updatedAt}, now()))
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DietRecordEntity entity);

    @Update("""
            UPDATE pet_diet_records
            SET food_id = #{foodId}, food_name = #{foodName}, weight = #{weight}, 
                meal_type = #{mealType}, meal_time = #{mealTime},
                estimated_kcal = #{estimatedKcal}, protein_g = #{proteinG}, 
                fat_g = #{fatG}, carb_g = #{carbG}, updated_at = now()
            WHERE id = #{id} AND deleted_at IS NULL
            """)
    int updateById(DietRecordEntity entity);

    @Update("""
            UPDATE pet_diet_records
            SET deleted_at = now(), updated_at = now()
            WHERE id = #{id}
            """)
    int softDeleteById(Long id);

    @Select("""
            SELECT COUNT(DISTINCT DATE(meal_time))
            FROM pet_diet_records
            WHERE pet_id = #{petId} 
              AND DATE(meal_time) <= #{date}
              AND deleted_at IS NULL
            """)
    int countDistinctDatesBeforeDate(@Param("petId") Long petId, @Param("date") LocalDate date);

    default DietRecordEntity save(DietRecordEntity entity) {
        if (entity.getId() == null) {
            insert(entity);
        } else {
            updateById(entity);
        }
        return entity;
    }
}
