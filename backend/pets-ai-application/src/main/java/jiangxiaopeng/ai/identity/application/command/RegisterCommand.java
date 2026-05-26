package jiangxiaopeng.ai.identity.application.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterCommand(
        @NotBlank String inviteCode,
        @NotBlank @Size(max = 64) String username,
        @Email String email,
        @Size(min = 8) String password
) {}
