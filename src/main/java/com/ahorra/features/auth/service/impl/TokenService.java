package com.ahorra.features.auth.service.impl;

import com.ahorra.common.exception.RefreshTokenInvalidException;
import com.ahorra.features.user.model.User;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
public class TokenService {

    private static final String CLAIM_TYPE = "typ";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";
    private static final int MIN_SECRET_BYTES = 32;

    public record TokenPair(String accessToken, String refreshToken, long expiresIn, long refreshExpiresIn) {
    }

    private final JwtEncoder encoder;
    private final NimbusJwtDecoder accessDecoder;
    private final NimbusJwtDecoder refreshDecoder;
    private final String issuer;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;

    public TokenService(@Value("${app.jwt.secret:}") String secret,
                        @Value("${app.jwt.issuer:ahorra}") String issuer,
                        @Value("${app.jwt.access-ttl-seconds:900}") long accessTtlSeconds,
                        @Value("${app.jwt.refresh-ttl-seconds:2592000}") long refreshTtlSeconds) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("app.jwt.secret (env JWT_SECRET) must be at least " + MIN_SECRET_BYTES + " characters long");
        }
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");

        this.issuer = issuer;
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        this.accessDecoder = decoderFor(key, ACCESS);
        this.refreshDecoder = decoderFor(key, REFRESH);
    }

    public TokenPair issueTokens(User user) {
        return new TokenPair(
                encode(user, ACCESS, accessTtlSeconds),
                encode(user, REFRESH, refreshTtlSeconds),
                accessTtlSeconds,
                refreshTtlSeconds);
    }

    public JwtDecoder accessTokenDecoder() {
        return accessDecoder;
    }

    public Long userIdFromRefreshToken(String refreshToken) {
        try {
            Jwt jwt = refreshDecoder.decode(refreshToken);
            return Long.valueOf(jwt.getSubject());
        } catch (JwtException | NumberFormatException ex) {
            throw new RefreshTokenInvalidException("Refresh token is invalid or expired", ex);
        }
    }

    private String encode(User user, String type, long ttlSeconds) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ttlSeconds))
                .subject(String.valueOf(user.getId()))
                .claim(CLAIM_TYPE, type)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private NimbusJwtDecoder decoderFor(SecretKey key, String expectedType) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        OAuth2TokenValidator<Jwt> typeCheck = jwt -> expectedType.equals(jwt.getClaimAsString(CLAIM_TYPE))
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Wrong token type", null));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(issuer), typeCheck));
        return decoder;
    }
}
