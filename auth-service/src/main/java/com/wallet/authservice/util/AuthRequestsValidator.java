package com.wallet.authservice.util;

import com.wallet.authservice.exception.IncorrectPasswordException;
import com.wallet.authservice.exception.PasswordMismatchException;
import com.wallet.authservice.exception.UserAlreadyExistsException;
import com.wallet.authservice.exception.UserNotFoundException;
import com.wallet.authservice.service.PasswordService;
import com.wallet.authservice.service.UnverifiedUserService;
import com.wallet.authservice.service.UserPrototypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AuthRequestsValidator {
    private final UnverifiedUserService unverifiedUserService;
    private final UserPrototypeService userPrototypeService;
    private final PasswordService passwordService;

    public void validateSignUpRequest(String password, String passwordConfirmation, String email, String phone) {
        if (!Objects.equals(password, passwordConfirmation)) {
            throw new PasswordMismatchException("Passwords dont' match");
        }
        if (unverifiedUserService.existsByEmailOrPhone(email, phone)) {
            throw new UserAlreadyExistsException("User with this email address or phone number already exists");
        }
    }

    public void validateSignInRequest(String email, String password) {
        if (!userPrototypeService.existsByEmail(email)) {
            throw new UserNotFoundException("User with this email doesn't exist");
        }

        String encodedPassword = userPrototypeService.findPasswordByEmail(email);
        if (!passwordService.matchesPassword(password, encodedPassword)) {
            throw new IncorrectPasswordException("Incorrect password");
        }
    }

    public void validateChangePasswordRequest(String email ,String oldPassword, String newPassword, String newPasswordConfirmation ) {
        String encodedPassword = userPrototypeService.findPasswordByEmail(email);
        if (!passwordService.matchesPassword(oldPassword, encodedPassword)) {
            throw new IncorrectPasswordException("Incorrect old password");
        }

        if (oldPassword.equals(newPassword)) {
            throw new IncorrectPasswordException("Old and new passwords are similar");
        }

        if (!newPassword.equals(newPasswordConfirmation)) {
            throw new IncorrectPasswordException("Passwords don't match");
        }
    }
}
