package jiangxiaopeng.ai.pet.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class Pet {

    private Long id;
    private Long userId;
    private String name;
    private PetSpecies species;
    private String breed;
    private LocalDate birthday;
    private BigDecimal weightKg;
    private Gender gender;
    private Boolean neutered;
    private Boolean microchipped;
    private String avatarUrl;
    private String avatarEmoji;
    private String allergies;
    private String chronicConditions;
    private String mainFoodBrand;
    private String vetHospital;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public Pet() {
    }

    public static Pet create(Long userId, String name, PetSpecies species) {
        Pet pet = new Pet();
        pet.userId = userId;
        pet.name = name;
        pet.species = species;
        pet.neutered = false;
        pet.microchipped = false;
        return pet;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void markDeleted() {
        this.deletedAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PetSpecies getSpecies() {
        return species;
    }

    public void setSpecies(PetSpecies species) {
        this.species = species;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Boolean getNeutered() {
        return neutered;
    }

    public void setNeutered(Boolean neutered) {
        this.neutered = neutered;
    }

    public Boolean getMicrochipped() {
        return microchipped;
    }

    public void setMicrochipped(Boolean microchipped) {
        this.microchipped = microchipped;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarEmoji() {
        return avatarEmoji;
    }

    public void setAvatarEmoji(String avatarEmoji) {
        this.avatarEmoji = avatarEmoji;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getChronicConditions() {
        return chronicConditions;
    }

    public void setChronicConditions(String chronicConditions) {
        this.chronicConditions = chronicConditions;
    }

    public String getMainFoodBrand() {
        return mainFoodBrand;
    }

    public void setMainFoodBrand(String mainFoodBrand) {
        this.mainFoodBrand = mainFoodBrand;
    }

    public String getVetHospital() {
        return vetHospital;
    }

    public void setVetHospital(String vetHospital) {
        this.vetHospital = vetHospital;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}
