package jiangxiaopeng.ai.diet.infrastructure.persistence;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface StapleFoodMapper {

    @Select("""
            SELECT id, name, brand, food_type, target_species, target_breed, target_age, target_size,
                   crude_protein_pct, crude_fat_pct, crude_ash_pct, crude_fiber_pct, moisture_pct,
                   net_weight_g, price_yuan, barcode, image_url, description, ingredients, feeding_guide,
                   created_at, updated_at
            FROM staple_foods
            WHERE id = #{id}
            """)
    Optional<StapleFoodEntity> findById(Long id);

    @Select("""
            SELECT id, name, brand, food_type, target_species, target_breed, target_age, target_size,
                   crude_protein_pct, crude_fat_pct, crude_ash_pct, crude_fiber_pct, moisture_pct,
                   net_weight_g, price_yuan, barcode, image_url, description, ingredients, feeding_guide,
                   created_at, updated_at
            FROM staple_foods
            WHERE name ILIKE '%' || #{keyword} || '%' 
               OR brand ILIKE '%' || #{keyword} || '%'
            ORDER BY brand ASC, name ASC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<StapleFoodEntity> searchByKeyword(@Param("keyword") String keyword, 
                                           @Param("limit") int limit, 
                                           @Param("offset") int offset);

    @Select("""
            SELECT id, name, brand, food_type, target_species, target_breed, target_age, target_size,
                   crude_protein_pct, crude_fat_pct, crude_ash_pct, crude_fiber_pct, moisture_pct,
                   net_weight_g, price_yuan, barcode, image_url, description, ingredients, feeding_guide,
                   created_at, updated_at
            FROM staple_foods
            WHERE brand = #{brand}
            ORDER BY name ASC
            """)
    List<StapleFoodEntity> findByBrand(String brand);

    @Select("""
            SELECT COUNT(*)
            FROM staple_foods
            WHERE name ILIKE '%' || #{keyword} || '%' 
               OR brand ILIKE '%' || #{keyword} || '%'
            """)
    int countByKeyword(String keyword);
}
