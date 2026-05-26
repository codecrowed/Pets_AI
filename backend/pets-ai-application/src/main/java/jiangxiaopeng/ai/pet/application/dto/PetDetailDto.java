package jiangxiaopeng.ai.pet.application.dto;

import jiangxiaopeng.ai.pet.domain.model.Pet;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * 宠物详情 DTO，包含所有字段
 */
public record PetDetailDto(
        Long id,
        String name,
        String species,
        String breed,
        LocalDate birthday,
        BigDecimal weightKg,
        String gender,
        Boolean neutered,
        Boolean microchipped,
        String avatarUrl,
        String avatarEmoji,
        String allergies,
        String chronicConditions,
        String mainFoodBrand,
        String vetHospital,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
    public static PetDetailDto fromDomain(Pet pet) {
        return new PetDetailDto(
                pet.getId(),
                pet.getName(),
                pet.getSpecies() != null ? pet.getSpecies().getValue() : null,
                pet.getBreed(),
                pet.getBirthday(),
                pet.getWeightKg(),
                pet.getGender() != null ? pet.getGender().getValue() : null,
                pet.getNeutered(),
                pet.getMicrochipped(),
                pet.getAvatarUrl(),
                pet.getAvatarEmoji(),
                pet.getAllergies(),
                pet.getChronicConditions(),
                pet.getMainFoodBrand(),
                pet.getVetHospital(),
                pet.getNotes(),
                pet.getCreatedAt(),
                pet.getUpdatedAt()
        );
    }
}
