package jiangxiaopeng.ai.conversation.application.service.auth;

import jiangxiaopeng.ai.identity.application.command.LoginCommand;
import jiangxiaopeng.ai.identity.application.command.RegisterCommand;
import jiangxiaopeng.ai.identity.application.dto.AuthResponse;
import jiangxiaopeng.ai.identity.application.service.AuthApplicationService;
import jiangxiaopeng.ai.identity.application.service.JwtTokenService;
import jiangxiaopeng.ai.identity.domain.model.InvitationCode;
import jiangxiaopeng.ai.identity.domain.model.User;
import jiangxiaopeng.ai.identity.domain.repository.InvitationCodeRepository;
import jiangxiaopeng.ai.identity.domain.repository.UserRepository;
import jiangxiaopeng.ai.identity.domain.service.PasswordEncoder;
import jiangxiaopeng.ai.shared.exception.BusinessException;
import jiangxiaopeng.ai.shared.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAuthTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private InvitationCodeRepository invitationCodeRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenService jwtTokenService;

    private AuthApplicationService service;

    private static final String INVITE_CODE = "INVITE-2024";
    private static final String USERNAME = "testuser";
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encoded-password-hash";
    private static final String ACCESS_TOKEN = "access-token-xxx";
    private static final String REFRESH_TOKEN = "refresh-token-xxx";
    private static final long ACCESS_TOKEN_EXPIRATION = 3600L;

    @BeforeEach
    void setUp() {
        service = new AuthApplicationService(
                userRepository, invitationCodeRepository,
                passwordEncoder, jwtTokenService
        );
    }

    private InvitationCode createValidInvitationCode() {
        InvitationCode code = new InvitationCode();
        code.setId(1L);
        code.setCode(INVITE_CODE);
        code.setStatus("ACTIVE");
        code.setMaxUses(10);
        code.setUsedCount(0);
        code.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        return code;
    }

    private User createExistingUser() {
        User user = User.createWithInviteCode(USERNAME, EMAIL, ENCODED_PASSWORD);
        user.setId(1L);
        return user;
    }

    // =========================================================================
    // register tests
    // =========================================================================

    @Nested
    @DisplayName("register")
    class RegisterTests {

        private RegisterCommand cmd;

        @BeforeEach
        void setUp() {
            cmd = new RegisterCommand(INVITE_CODE, USERNAME, EMAIL, PASSWORD);
        }

        private void stubRegisterHappyPath() {
            InvitationCode invitationCode = createValidInvitationCode();
            when(invitationCodeRepository.findByCode(INVITE_CODE)).thenReturn(Optional.of(invitationCode));
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User user = inv.getArgument(0);
                user.setId(1L);
                return user;
            });
            when(invitationCodeRepository.save(any(InvitationCode.class))).thenAnswer(inv -> inv.getArgument(0));
            when(jwtTokenService.generateAccessToken(any(User.class))).thenReturn(ACCESS_TOKEN);
            when(jwtTokenService.generateRefreshToken(any(User.class))).thenReturn(REFRESH_TOKEN);
            when(jwtTokenService.getAccessTokenExpiration()).thenReturn(ACCESS_TOKEN_EXPIRATION);
        }

        @Test
        @DisplayName("happy path returns AuthResponse with tokens and user info")
        void happyPath_returnsAuthResponse() {
            stubRegisterHappyPath();

            AuthResponse response = service.register(cmd);

            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
            assertThat(response.expiresIn()).isEqualTo(ACCESS_TOKEN_EXPIRATION);
            assertThat(response.user()).isNotNull();
            assertThat(response.user().username()).isEqualTo(USERNAME);
            assertThat(response.user().email()).isEqualTo(EMAIL);
            assertThat(response.user().plan()).isEqualTo("FREE");
            assertThat(response.user().uid()).isNotNull();
        }

        @Test
        @DisplayName("happy path saves user with encoded password")
        void happyPath_savesUserWithEncodedPassword() {
            stubRegisterHappyPath();

            service.register(cmd);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            User savedUser = captor.getValue();
            assertThat(savedUser.getPasswordHash()).isEqualTo(ENCODED_PASSWORD);
            assertThat(savedUser.getUsername()).isEqualTo(USERNAME);
            assertThat(savedUser.getEmail()).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("happy path uses and saves invitation code with incremented usedCount")
        void happyPath_usesAndSavesInvitationCode() {
            stubRegisterHappyPath();

            service.register(cmd);

            ArgumentCaptor<InvitationCode> captor = ArgumentCaptor.forClass(InvitationCode.class);
            verify(invitationCodeRepository).save(captor.capture());

            InvitationCode savedCode = captor.getValue();
            assertThat(savedCode.getUsedCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("invite code not found throws AUTH_001")
        void inviteCodeNotFound_throwsAUTH_001() {
            when(invitationCodeRepository.findByCode(INVITE_CODE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.register(cmd))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.AUTH_001));
        }

        @Test
        @DisplayName("invite code inactive throws AUTH_001")
        void inviteCodeInactive_throwsAUTH_001() {
            InvitationCode code = createValidInvitationCode();
            code.setStatus("DISABLED");
            when(invitationCodeRepository.findByCode(INVITE_CODE)).thenReturn(Optional.of(code));

            assertThatThrownBy(() -> service.register(cmd))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.AUTH_001));
        }

        @Test
        @DisplayName("invite code expired throws AUTH_001")
        void inviteCodeExpired_throwsAUTH_001() {
            InvitationCode code = createValidInvitationCode();
            code.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));
            when(invitationCodeRepository.findByCode(INVITE_CODE)).thenReturn(Optional.of(code));

            assertThatThrownBy(() -> service.register(cmd))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.AUTH_001));
        }

        @Test
        @DisplayName("invite code exhausted throws AUTH_001")
        void inviteCodeExhausted_throwsAUTH_001() {
            InvitationCode code = createValidInvitationCode();
            code.setUsedCount(10);
            code.setMaxUses(10);
            when(invitationCodeRepository.findByCode(INVITE_CODE)).thenReturn(Optional.of(code));

            assertThatThrownBy(() -> service.register(cmd))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.AUTH_001));
        }

        @Test
        @DisplayName("email already exists throws AUTH_004")
        void emailAlreadyExists_throwsAUTH_004() {
            InvitationCode code = createValidInvitationCode();
            when(invitationCodeRepository.findByCode(INVITE_CODE)).thenReturn(Optional.of(code));
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

            assertThatThrownBy(() -> service.register(cmd))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.AUTH_004));
        }
    }

    // =========================================================================
    // login tests
    // =========================================================================

    @Nested
    @DisplayName("login")
    class LoginTests {

        private LoginCommand cmd;

        @BeforeEach
        void setUp() {
            cmd = new LoginCommand(EMAIL, PASSWORD);
        }

        private void stubLoginHappyPath() {
            User user = createExistingUser();
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(jwtTokenService.generateAccessToken(any(User.class))).thenReturn(ACCESS_TOKEN);
            when(jwtTokenService.generateRefreshToken(any(User.class))).thenReturn(REFRESH_TOKEN);
            when(jwtTokenService.getAccessTokenExpiration()).thenReturn(ACCESS_TOKEN_EXPIRATION);
        }

        @Test
        @DisplayName("happy path returns AuthResponse")
        void happyPath_returnsAuthResponse() {
            stubLoginHappyPath();

            AuthResponse response = service.login(cmd);

            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
            assertThat(response.expiresIn()).isEqualTo(ACCESS_TOKEN_EXPIRATION);
            assertThat(response.user()).isNotNull();
            assertThat(response.user().username()).isEqualTo(USERNAME);
            assertThat(response.user().email()).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("happy path records login and saves user")
        void happyPath_recordsLoginAndSaves() {
            stubLoginHappyPath();

            service.login(cmd);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            User savedUser = captor.getValue();
            assertThat(savedUser.getLastLoginAt()).isNotNull();
        }

        @Test
        @DisplayName("user not found throws AUTH_003")
        void userNotFound_throwsAUTH_003() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.login(cmd))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.AUTH_003));
        }

        @Test
        @DisplayName("wrong password throws AUTH_003")
        void wrongPassword_throwsAUTH_003() {
            User user = createExistingUser();
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

            assertThatThrownBy(() -> service.login(cmd))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.AUTH_003));
        }
    }

    // =========================================================================
    // logout tests
    // =========================================================================

    @Nested
    @DisplayName("logout")
    class LogoutTests {

        @Test
        @DisplayName("happy path blacklists both tokens")
        void happyPath_blacklistsBothTokens() {
            service.logout(ACCESS_TOKEN, REFRESH_TOKEN);

            verify(jwtTokenService).blacklistToken(ACCESS_TOKEN);
            verify(jwtTokenService).blacklistToken(REFRESH_TOKEN);
            verify(jwtTokenService, times(2)).blacklistToken(any());
        }

        @Test
        @DisplayName("null access token only blacklists refresh token")
        void nullAccessToken_onlyBlacklistsRefreshToken() {
            service.logout(null, REFRESH_TOKEN);

            verify(jwtTokenService).blacklistToken(REFRESH_TOKEN);
            verify(jwtTokenService, times(1)).blacklistToken(any());
        }

        @Test
        @DisplayName("null refresh token only blacklists access token")
        void nullRefreshToken_onlyBlacklistsAccessToken() {
            service.logout(ACCESS_TOKEN, null);

            verify(jwtTokenService).blacklistToken(ACCESS_TOKEN);
            verify(jwtTokenService, times(1)).blacklistToken(any());
        }

        @Test
        @DisplayName("both null results in no blacklisting")
        void bothNull_noBlacklisting() {
            service.logout(null, null);

            verify(jwtTokenService, never()).blacklistToken(any());
        }
    }
}
