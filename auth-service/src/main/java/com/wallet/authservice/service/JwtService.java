package com.wallet.authservice.service;

import com.wallet.authservice.dto.JwtAuthenticationResponse;
import com.wallet.authservice.entity.UserPrototype;
import com.wallet.authservice.exception.JwtTokenExpiredException;
import com.wallet.authservice.util.Generator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${security.jwt.secret}")
    private String jwtSigningKey;

    @Value("${security.jwt.expirationMs}")
    private int accessTokenTtl;

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String getJwtAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof UserPrototype customUserDetails) {
            claims.put("id", customUserDetails.getId());
            claims.put("email", customUserDetails.getEmail());
        }
        return getJwtAccessToken(claims, userDetails);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    public Claims extractAllClaims(String accessToken) {
        return checkExpiredToken(accessToken);
    }

    private Claims checkExpiredToken(String accessToken) {
        try {
            return Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtTokenExpiredException(e.getMessage());
        }
    }

    private String getJwtAccessToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenTtl))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    public String getJwtRefreshToken(String accessToken) {
        String randomString = Generator.generateRandomString(20);
        String lastSixCharacters = Generator.getLastSixCharacters(accessToken);
        return randomString + lastSixCharacters;
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public JwtAuthenticationResponse getJwtAuthenticationResponse(String accessToken, String refreshToken) {
        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
