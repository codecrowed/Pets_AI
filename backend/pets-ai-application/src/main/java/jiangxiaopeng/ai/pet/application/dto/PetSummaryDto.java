package jiangxiaopeng.ai.pet.application.dto;

import jiangxiaopeng.ai.pet.domain.model.Pet;

import java.time.LocalDate;

/**
 * 宠物摘要 DTO，用于列表展示
 */
public record PetSummaryDto(
        Long id,
        String name,
        String species,
        String breed,
        String avatarEmoji,
        String avatarUrl,
        String gender,
        LocalDate birthday,
        Boolean neutered,
        Boolean microchipped
) {
    public static PetSummaryDto fromDomain(Pet pet) {
        return new PetSummaryDto(
                pet.getId(),
                pet.getName(),
                pet.getSpecies() != null ? pet.getSpecies().getValue() : null,
                pet.getBreed(),
                pet.getAvatarEmoji(),
                pet.getAvatarUrl(),
                pet.getGender() != null ? pet.getGender().getValue() : null,
                pet.getBirthday(),
                pet.getNeutered(),
                pet.getMicrochipped()
        );
    }
}
