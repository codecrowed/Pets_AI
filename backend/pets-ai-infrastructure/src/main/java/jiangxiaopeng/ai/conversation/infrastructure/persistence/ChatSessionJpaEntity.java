package jiangxiaopeng.ai.conversation.infrastructure.persistence;

import jiangxiaopeng.ai.shared.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@Table(name = "chat_sessions")
@Setter
@Getter
public class ChatSessionJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false, unique = true)
    private String chatId;

    @Column(name = "uid", nullable = false, unique = true)
    private Long uid;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "status", nullable = false)
    private String status;

}
