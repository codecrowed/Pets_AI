package jiangxiaopeng.ai.ai.infrastructure.agent.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "pet_ai_client_config")
public class PetAiClientConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_config_id", nullable = false)
    private Long clientConfigId;

    @Column(name = "base_url", nullable = false)
    private String baseUrl;

    @Column(name = "api_key", nullable = false)
    private String apiKey;

    @Column(name = "completions_path", nullable = false)
    private String completionsPath;

    @Column(name = "embeddings_path", nullable = false)
    private String embeddingsPath;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "status", nullable = false)
    private Short status;

    @Column(name = "creator", nullable = false)
    private String creator;

    @Column(name = "delete_flag", nullable = false)
    private Short deleteFlag;

    public Long getClientConfigId() {
        return clientConfigId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getCompletionsPath() {
        return completionsPath;
    }

    public String getEmbeddingsPath() {
        return embeddingsPath;
    }

    public String getModel() {
        return model;
    }
}
