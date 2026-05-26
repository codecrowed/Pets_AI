package jiangxiaopeng.ai.pet.infrastructure.persistence;

import org.apache.ibatis.annotations.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Mapper
public interface PetJpaRepository {

    String SELECT_COLUMNS = """
            id, user_id, name, species, breed, birthday, weight_kg, gender, neutered, microchipped,
            avatar_url, avatar_emoji, allergies, chronic_conditions, main_food_brand, vet_hospital,
            notes, created_at, updated_at, deleted_at
            """;

    @Select("SELECT " + SELECT_COLUMNS + " FROM pets WHERE id = #{id} AND deleted_at IS NULL LIMIT 1")
    Optional<PetJpaEntity> findById(Long id);

    @Select("SELECT " + SELECT_COLUMNS + " FROM pets WHERE id = #{id} AND user_id = #{userId} AND deleted_at IS NULL LIMIT 1")
    Optional<PetJpaEntity> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT " + SELECT_COLUMNS + " FROM pets WHERE user_id = #{userId} AND deleted_at IS NULL ORDER BY created_at DESC")
    List<PetJpaEntity> findByUserId(Long userId);

    @Insert("""
            INSERT INTO pets(user_id, name, species, breed, birthday, weight_kg, gender, neutered, microchipped,
                avatar_url, avatar_emoji, allergies, chronic_conditions, main_food_brand, vet_hospital, notes,
                created_at, updated_at)
            VALUES(#{userId}, #{name}, #{species}, #{breed}, #{birthday}, #{weightKg}, #{gender}, #{neutered}, #{microchipped},
                #{avatarUrl}, #{avatarEmoji}, #{allergies}, #{chronicConditions}, #{mainFoodBrand}, #{vetHospital}, #{notes},
                COALESCE(#{createdAt}, now()), COALESCE(#{updatedAt}, now()))
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PetJpaEntity entity);

    @Update("""
            UPDATE pets SET
                name = #{name},
                species = #{species},
                breed = #{breed},
                birthday = #{birthday},
                weight_kg = #{weightKg},
                gender = #{gender},
                neutered = #{neutered},
                microchipped = #{microchipped},
                avatar_url = #{avatarUrl},
                avatar_emoji = #{avatarEmoji},
                allergies = #{allergies},
                chronic_conditions = #{chronicConditions},
                main_food_brand = #{mainFoodBrand},
                vet_hospital = #{vetHospital},
                notes = #{notes},
                updated_at = now()
            WHERE id = #{id}
            """)
    int updateById(PetJpaEntity entity);

    @Update("UPDATE pets SET deleted_at = #{deletedAt}, updated_at = now() WHERE id = #{id}")
    int softDelete(@Param("id") Long id, @Param("deletedAt") Instant deletedAt);

    default PetJpaEntity save(PetJpaEntity entity) {
        if (entity.getId() == null) {
            insert(entity);
        } else if (findById(entity.getId()).isPresent()) {
            updateById(entity);
        } else {
            insert(entity);
        }
        return entity;
    }
}
