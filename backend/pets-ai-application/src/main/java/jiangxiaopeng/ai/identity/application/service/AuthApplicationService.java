package jiangxiaopeng.ai.identity.application.service;

import jiangxiaopeng.ai.identity.application.command.LoginCommand;
import jiangxiaopeng.ai.identity.application.command.RefreshTokenCommand;
import jiangxiaopeng.ai.identity.application.command.RegisterCommand;
import jiangxiaopeng.ai.identity.application.dto.AuthResponse;
import jiangxiaopeng.ai.identity.application.dto.UserInfoDto;
import jiangxiaopeng.ai.identity.domain.model.InvitationCode;
import jiangxiaopeng.ai.identity.domain.model.User;
import jiangxiaopeng.ai.identity.domain.repository.InvitationCodeRepository;
import jiangxiaopeng.ai.identity.domain.repository.UserRepository;
import jiangxiaopeng.ai.identity.domain.service.PasswordEncoder;
import jiangxiaopeng.ai.shared.exception.BusinessException;
import jiangxiaopeng.ai.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthApplicationService {

    private final UserRepository userRepository;
    private final InvitationCodeRepository invitationCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthApplicationService(
            UserRepository userRepository,
            InvitationCodeRepository invitationCodeRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.invitationCodeRepository = invitationCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    public AuthResponse register(RegisterCommand command) {
        // 1. Validate invitation code
        InvitationCode invitationCode = invitationCodeRepository.findByCode(command.inviteCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_001));
        invitationCode.validate();

        // 2. Check email uniqueness
        if (command.email() != null && userRepository.existsByEmail(command.email())) {
            throw new BusinessException(ErrorCode.AUTH_004);
        }

        // 3. Create user
        String passwordHash = passwordEncoder.encode(command.password());
        User user = User.createWithInviteCode(command.username(), command.email(), passwordHash);
        user = userRepository.save(user);

        // 4. Mark invitation code as used
        invitationCode.use();
        invitationCodeRepository.save(invitationCode);

        // 5. Generate tokens
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_003));

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_003);
        }

        user.recordLogin();
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    public AuthResponse refreshToken(RefreshTokenCommand command) {
        if (!jwtTokenService.isTokenValid(command.refreshToken())) {
            throw new BusinessException(ErrorCode.AUTH_005);
        }

        Long userId = jwtTokenService.getUserIdFromToken(command.refreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));

        return buildAuthResponse(user);
    }

    public void logout(String accessToken, String refreshToken) {
        if (accessToken != null) {
            jwtTokenService.blacklistToken(accessToken);
        }
        if (refreshToken != null) {
            jwtTokenService.blacklistToken(refreshToken);
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtTokenService.generateAccessToken(user);
        String refreshToken = jwtTokenService.generateRefreshToken(user);
        UserInfoDto userInfo = new UserInfoDto(
                user.getUid(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getPlan().name()
        );
        return new AuthResponse(accessToken, refreshToken, jwtTokenService.getAccessTokenExpiration(), userInfo);
    }
}
