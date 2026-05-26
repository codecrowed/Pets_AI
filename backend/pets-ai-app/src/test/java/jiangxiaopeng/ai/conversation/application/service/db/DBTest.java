package jiangxiaopeng.ai.conversation.application.service.db;

import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import jiangxiaopeng.ai.identity.domain.model.InvitationCode;
import jiangxiaopeng.ai.identity.domain.repository.InvitationCodeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
public class DBTest {

    @Resource
    private InvitationCodeRepository invitationCodeRepository;

    @Test
    public void test_invitation() {
        Optional<InvitationCode> invitationCode = invitationCodeRepository.findByCode("JYG_INVITE_0316");

        assert invitationCode.isPresent();

        System.out.println(JSONObject.toJSONString(invitationCode.get()));
    }
}
