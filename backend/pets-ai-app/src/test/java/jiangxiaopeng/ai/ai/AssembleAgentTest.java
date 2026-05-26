package jiangxiaopeng.ai.ai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.alibaba.fastjson.JSONObject;

import jakarta.annotation.Resource;
import jiangxiaopeng.ai.ai.infrastructure.agent.AgentPromptContext;
import jiangxiaopeng.ai.ai.infrastructure.agent.PetAiAgentRuntime;
import jiangxiaopeng.ai.ai.infrastructure.agent.PetAiAgentRuntimeAssembler;

@SpringBootTest
public class AssembleAgentTest {

    @Resource
    private PetAiAgentRuntimeAssembler petAiAgentConfigEntity;
    
    @Test
    public void test_assemble_agent(){
        System.out.println("开始执行测试.....");
        PetAiAgentRuntime assembleMainAgent = petAiAgentConfigEntity.assembleMainAgent(new AgentPromptContext(
            "1234567890",
            "用户消息",
            20000001L, 
            "意图识别Agent", 
            "负责识别用户意图和任务分发"));
            
        System.out.println(JSONObject.toJSONString(assembleMainAgent));
    }
}
