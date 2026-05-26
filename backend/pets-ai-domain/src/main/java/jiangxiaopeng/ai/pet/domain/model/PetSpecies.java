package jiangxiaopeng.ai.pet.domain.model;

public enum PetSpecies {
    DOG("dog"),
    CAT("cat");

    private final String value;

    PetSpecies(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PetSpecies fromValue(String value) {
        for (PetSpecies species : values()) {
            if (species.value.equalsIgnoreCase(value)) {
                return species;
            }
        }
        throw new IllegalArgumentException("Unknown pet species: " + value);
    }
}
