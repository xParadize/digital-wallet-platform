package com.wallet.authservice.service;

import com.wallet.authservice.dto.JwtAuthenticationResponse;
import com.wallet.authservice.dto.SignInRequest;
import com.wallet.authservice.entity.UnverifiedUser;
import com.wallet.authservice.exception.ConfirmationTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UnverifiedUserService unverifiedUserService;

    @Transactional
    public UnverifiedUser confirmEmailToken(String code) {
        UnverifiedUser unverifiedUser = unverifiedUserService.getUnverifiedUserById(UUID.fromString(code))
                .orElseThrow(ConfirmationTokenException::new);
        return unverifiedUser;
    }

//    public JwtAuthenticationResponse signIn(SignInRequest request) {
//        Authentication authentication = authenticationManager
//                .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//        String jwt = jwtService.generateToken(userDetails);
//        User tempUser = userService.findByUsername(request.getUsername());
//        RefreshToken userRefreshToken = refreshTokenService.findRefreshTokenByUserId(tempUser.getId());
//        var refreshToken = userRefreshToken.getToken();
//
//        JwtAuthenticationResponse response = JwtAuthenticationResponse.builder()
//                .accessToken(jwt)
//                .refreshToken(refreshToken)
//                .build();
//
//        return response;
//    }

}
