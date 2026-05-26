package jiangxiaopeng.ai.shared.domain.vo;

import java.util.Objects;
import java.util.UUID;

public record Uid(String value) {
    public Uid {
        Objects.requireNonNull(value, "Uid cannot be null");
    }

    public static Uid generate() {
        return new Uid(UUID.randomUUID().toString());
    }
}
