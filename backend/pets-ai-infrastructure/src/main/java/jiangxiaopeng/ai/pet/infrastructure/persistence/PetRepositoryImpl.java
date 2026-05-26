package jiangxiaopeng.ai.pet.infrastructure.persistence;

import jiangxiaopeng.ai.pet.domain.model.Gender;
import jiangxiaopeng.ai.pet.domain.model.Pet;
import jiangxiaopeng.ai.pet.domain.model.PetSpecies;
import jiangxiaopeng.ai.pet.domain.repository.PetRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class PetRepositoryImpl implements PetRepository {

    private final PetJpaRepository jpaRepository;

    public PetRepositoryImpl(PetJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Pet save(Pet pet) {
        PetJpaEntity entity = toEntity(pet);
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<Pet> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Pet> findByIdAndUserId(Long id, Long userId) {
        return jpaRepository.findByIdAndUserId(id, userId).map(this::toDomain);
    }

    @Override
    public List<Pet> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void softDelete(Long id) {
        jpaRepository.softDelete(id, Instant.now());
    }

    private PetJpaEntity toEntity(Pet pet) {
        PetJpaEntity entity = new PetJpaEntity();
        entity.setId(pet.getId());
        entity.setUserId(pet.getUserId());
        entity.setName(pet.getName());
        entity.setSpecies(pet.getSpecies() != null ? pet.getSpecies().getValue() : null);
        entity.setBreed(pet.getBreed());
        entity.setBirthday(pet.getBirthday());
        entity.setWeightKg(pet.getWeightKg());
        entity.setGender(pet.getGender() != null ? pet.getGender().getValue() : null);
        entity.setNeutered(pet.getNeutered());
        entity.setMicrochipped(pet.getMicrochipped());
        entity.setAvatarUrl(pet.getAvatarUrl());
        entity.setAvatarEmoji(pet.getAvatarEmoji());
        entity.setAllergies(pet.getAllergies());
        entity.setChronicConditions(pet.getChronicConditions());
        entity.setMainFoodBrand(pet.getMainFoodBrand());
        entity.setVetHospital(pet.getVetHospital());
        entity.setNotes(pet.getNotes());
        entity.setCreatedAt(pet.getCreatedAt());
        entity.setUpdatedAt(pet.getUpdatedAt());
        entity.setDeletedAt(pet.getDeletedAt());
        return entity;
    }

    private Pet toDomain(PetJpaEntity entity) {
        Pet pet = new Pet();
        pet.setId(entity.getId());
        pet.setUserId(entity.getUserId());
        pet.setName(entity.getName());
        pet.setSpecies(entity.getSpecies() != null ? PetSpecies.fromValue(entity.getSpecies()) : null);
        pet.setBreed(entity.getBreed());
        pet.setBirthday(entity.getBirthday());
        pet.setWeightKg(entity.getWeightKg());
        pet.setGender(entity.getGender() != null ? Gender.fromValue(entity.getGender()) : null);
        pet.setNeutered(entity.getNeutered());
        pet.setMicrochipped(entity.getMicrochipped());
        pet.setAvatarUrl(entity.getAvatarUrl());
        pet.setAvatarEmoji(entity.getAvatarEmoji());
        pet.setAllergies(entity.getAllergies());
        pet.setChronicConditions(entity.getChronicConditions());
        pet.setMainFoodBrand(entity.getMainFoodBrand());
        pet.setVetHospital(entity.getVetHospital());
        pet.setNotes(entity.getNotes());
        pet.setCreatedAt(entity.getCreatedAt());
        pet.setUpdatedAt(entity.getUpdatedAt());
        pet.setDeletedAt(entity.getDeletedAt());
        return pet;
    }
}
