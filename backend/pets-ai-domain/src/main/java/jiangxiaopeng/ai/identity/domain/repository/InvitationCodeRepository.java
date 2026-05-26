package jiangxiaopeng.ai.identity.domain.repository;

import jiangxiaopeng.ai.identity.domain.model.InvitationCode;

import java.util.Optional;

public interface InvitationCodeRepository {
    Optional<InvitationCode> findByCode(String code);
    InvitationCode save(InvitationCode invitationCode);
}
