package jiangxiaopeng.ai.storage.domain.model;

import java.util.Objects;

public record StorageKey(String value) {
    public StorageKey {
        Objects.requireNonNull(value);
    }
}
