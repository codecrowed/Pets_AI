package jiangxiaopeng.ai.shared.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jiangxiaopeng.ai.pet.domain.model.Pet;
import jiangxiaopeng.ai.pet.domain.repository.PetRepository;
import jiangxiaopeng.ai.shared.context.PetContext;
import jiangxiaopeng.ai.shared.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

/**
 * 宠物上下文拦截器
 * <p>
 * 从请求头 `X-Pet-Id` 中读取当前选择的宠物 ID，
 * 查询宠物信息并设置到 RequestContext 中。
 * <p>
 * 前端需要在请求时添加头：
 * <pre>
 * X-Pet-Id: 123
 * </pre>
 */
@Component
public class PetContextInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(PetContextInterceptor.class);
    private static final String PET_ID_HEADER = "X-Pet-Id";

    private final PetRepository petRepository;

    public PetContextInterceptor(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String petIdStr = request.getHeader(PET_ID_HEADER);
        if (!StringUtils.hasText(petIdStr)) {
            return true;
        }

        Long petId;
        try {
            petId = Long.parseLong(petIdStr.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid X-Pet-Id header: {}", petIdStr);
            return true;
        }

        Optional<Long> currentUserId = RequestContext.currentUserId();
        if (currentUserId.isEmpty()) {
            return true;
        }

        petRepository.findByIdAndUserId(petId, currentUserId.get())
                .ifPresent(pet -> {
                    PetContext petContext = PetContext.of(
                            pet.getId(),
                            pet.getName(),
                            pet.getSpecies() != null ? pet.getSpecies().getValue() : null,
                            pet.getBreed()
                    );
                    RequestContext.setPet(petContext);
                    log.debug("Set pet context: petId={}, petName={}", pet.getId(), pet.getName());
                });

        return true;
    }
}
