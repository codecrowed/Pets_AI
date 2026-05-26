package jiangxiaopeng.ai.identity.application.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginCommand(
        @NotBlank @Email String email,
        @NotBlank String password
) {}
