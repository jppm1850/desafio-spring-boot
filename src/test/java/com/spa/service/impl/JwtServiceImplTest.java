package com.spa.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.spa.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

public class JwtServiceImplTest {

    private JwtService jwtService;
    private UserDetails userDetails;
    private final String SECRET_KEY = "testSecretKeyThatIsAtLeast32BytesLongForHS256Algorithm";
    private final long EXPIRATION = 86400000; // 1 day in milliseconds

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION);

        userDetails = User.builder()
                .username("usuario1")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void generateToken_DeberiaGenerarTokenValido() {
        // When
        String token = jwtService.generateToken(userDetails);

        // Then
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(jwtService.validateToken(token));
        assertEquals("usuario1", jwtService.extractUsername(token));
    }

    @Test
    void generateToken_ConClaims_DeberiaIncluirClaimsEnElToken() {
        // Given
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", 1L);
        extraClaims.put("role", "USER");

        // When
        String token = jwtService.generateToken(extraClaims, userDetails);

        // Then
        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));

        // Extraer y verificar userId como Long
        Long userId = jwtService.extractClaim(token, claims -> claims.get("userId", Long.class));
        assertEquals(1L, userId.longValue());

        // Extraer y verificar role como String
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        assertEquals("USER", role);
    }

    @Test
    void validateToken_ConTokenValido_DeberiaRetornarTrue() {
        // Given
        String token = jwtService.generateToken(userDetails);

        // When
        boolean isValid = jwtService.validateToken(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_ConTokenDeOtroUsuario_DeberiaRetornarFalse() {
        // Given
        String token = jwtService.generateToken(userDetails);

        UserDetails otherUser = User.builder()
                .username("otroUsuario")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // When
        boolean isValid = jwtService.validateToken(token, otherUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    void extractUserId_CuandoExisteEnElToken_DeberiaRetornarElId() {
        // Given
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", 1L);
        String token = jwtService.generateToken(extraClaims, userDetails);

        // When
        Long userId = jwtService.extractUserId(token);

        // Then
        assertNotNull(userId);
        assertEquals(1L, userId.longValue());
    }

    @Test
    void extractUserId_CuandoNoExisteEnElToken_DeberiaRetornarNull() {
        // Given
        String token = jwtService.generateToken(userDetails);

        // When
        Long userId = jwtService.extractUserId(token);

        // Then
        assertNull(userId);
    }

    @Test
    void validateToken_ConTokenInvalido_DeberiaRetornarFalse() {
        // Given
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        // When
        boolean isValid = jwtService.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }
}