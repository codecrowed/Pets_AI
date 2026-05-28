package jiangxiaopeng.ai.ai.infrastructure.agent.model;

public record HttpClientConfigRecord(
    int readTimeout,
    int connectTimeout
) {
    
}
