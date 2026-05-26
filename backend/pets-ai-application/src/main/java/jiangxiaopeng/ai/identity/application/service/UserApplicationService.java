package jiangxiaopeng.ai.identity.application.service;

import jiangxiaopeng.ai.identity.application.dto.UserInfoDto;
import jiangxiaopeng.ai.identity.domain.model.User;
import jiangxiaopeng.ai.identity.domain.repository.UserRepository;
import jiangxiaopeng.ai.identity.domain.service.PasswordEncoder;
import jiangxiaopeng.ai.shared.exception.BusinessException;
import jiangxiaopeng.ai.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserApplicationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserApplicationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserInfoDto getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));
        return new UserInfoDto(
                user.getUid().value(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getPlan().name()
        );
    }

    @Transactional
    public UserInfoDto updateUser(Long userId, String username, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));

        if (email != null && !email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.AUTH_004);
        }

        user.updateProfile(username, email);
        user = userRepository.save(user);

        return new UserInfoDto(
                user.getUid().value(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getPlan().name()
        );
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.USER_002);
        }

        user.changePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public UserInfoDto updateAvatar(Long userId, String avatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));
        user.updateAvatar(avatarUrl);
        user = userRepository.save(user);
        return new UserInfoDto(
                user.getUid().value(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getPlan().name()
        );
    }
}
