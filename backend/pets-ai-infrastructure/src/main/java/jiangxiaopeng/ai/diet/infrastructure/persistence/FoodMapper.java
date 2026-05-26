package jiangxiaopeng.ai.diet.infrastructure.persistence;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface FoodMapper {

    @Select("""
            SELECT id, name, icon, category, is_staple_food, staple_food_id, 
                   kcal_per_100g, protein_per_100g, fat_per_100g, carb_per_100g,
                   calcium_mg, phosphorus_mg, sodium_mg, iron_mg, zinc_mg,
                   moisture_pct, taurine_mg, omega3_pct, omega6_pct, description,
                   created_at, updated_at
            FROM foods
            WHERE id = #{id}
            """)
    Optional<FoodEntity> findById(Long id);

    @Select("""
            SELECT id, name, icon, category, is_staple_food, staple_food_id, 
                   kcal_per_100g, protein_per_100g, fat_per_100g, carb_per_100g,
                   calcium_mg, phosphorus_mg, sodium_mg, iron_mg, zinc_mg,
                   moisture_pct, taurine_mg, omega3_pct, omega6_pct, description,
                   created_at, updated_at
            FROM foods
            WHERE name ILIKE '%' || #{keyword} || '%'
            ORDER BY is_staple_food DESC, name ASC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<FoodEntity> searchByKeyword(@Param("keyword") String keyword, 
                                     @Param("limit") int limit, 
                                     @Param("offset") int offset);

    @Select("""
            SELECT COUNT(*)
            FROM foods
            WHERE name ILIKE '%' || #{keyword} || '%'
            """)
    int countByKeyword(String keyword);

    @Select("""
            SELECT f.id, f.name, f.icon, f.category, f.is_staple_food, f.staple_food_id, 
                   f.kcal_per_100g, f.protein_per_100g, f.fat_per_100g, f.carb_per_100g,
                   f.calcium_mg, f.phosphorus_mg, f.sodium_mg, f.iron_mg, f.zinc_mg,
                   f.moisture_pct, f.taurine_mg, f.omega3_pct, f.omega6_pct, f.description,
                   f.created_at, f.updated_at
            FROM foods f
            JOIN user_frequent_foods uf ON f.id = uf.food_id
            WHERE uf.user_id = #{userId}
            ORDER BY uf.use_count DESC, uf.last_used_at DESC
            LIMIT #{limit}
            """)
    List<FoodEntity> findFrequentByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Insert("""
            INSERT INTO user_frequent_foods (user_id, food_id, use_count, last_used_at)
            VALUES (#{userId}, #{foodId}, 1, now())
            ON CONFLICT (user_id, food_id)
            DO UPDATE SET use_count = user_frequent_foods.use_count + 1, last_used_at = now()
            """)
    int incrementUserFoodUsage(@Param("userId") Long userId, @Param("foodId") Long foodId);
}
