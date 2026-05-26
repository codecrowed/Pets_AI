package jiangxiaopeng.ai.pet.domain.repository;

import jiangxiaopeng.ai.pet.domain.model.Pet;

import java.util.List;
import java.util.Optional;

public interface PetRepository {

    Pet save(Pet pet);

    Optional<Pet> findById(Long id);

    Optional<Pet> findByIdAndUserId(Long id, Long userId);

    List<Pet> findByUserId(Long userId);

    void softDelete(Long id);
}
