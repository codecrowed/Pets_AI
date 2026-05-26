package jiangxiaopeng.ai.identity.infrastructure.persistence;

import jiangxiaopeng.ai.identity.domain.model.InvitationCode;
import jiangxiaopeng.ai.identity.domain.repository.InvitationCodeRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class InvitationCodeRepositoryImpl implements InvitationCodeRepository {

    private final InvitationCodeJpaRepository jpaRepository;

    public InvitationCodeRepositoryImpl(InvitationCodeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<InvitationCode> findByCode(String code) {
        return jpaRepository.findByCode(code).map(this::toDomain);
    }

    @Override
    public InvitationCode save(InvitationCode domain) {
        InvitationCodeJpaEntity entity = toEntity(domain);
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    private InvitationCodeJpaEntity toEntity(InvitationCode domain) {
        InvitationCodeJpaEntity entity = new InvitationCodeJpaEntity();
        entity.setId(domain.getId());
        entity.setCode(domain.getCode());
        entity.setMaxUses(domain.getMaxUses());
        entity.setUsedCount(domain.getUsedCount());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    private InvitationCode toDomain(InvitationCodeJpaEntity entity) {
        InvitationCode code = new InvitationCode();
        code.setId(entity.getId());
        code.setCode(entity.getCode());
        code.setMaxUses(entity.getMaxUses());
        code.setUsedCount(entity.getUsedCount());
        code.setExpiresAt(entity.getExpiresAt());
        code.setStatus(entity.getStatus());
        code.setCreatedAt(entity.getCreatedAt());
        return code;
    }
}
