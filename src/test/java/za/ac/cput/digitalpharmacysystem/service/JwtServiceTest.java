package za.ac.cput.digitalpharmacysystem.service;

import org.junit.jupiter.api.Test;
import za.ac.cput.digitalpharmacysystem.domain.User;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=", 3_600_000);

    @Test
    void generatedTokenContainsTheAuthenticatedUserIdentityAndRole() {
        User user = new User.Builder()
                .setId(10L)
                .setFullName("Test Pharmacist")
                .setEmail("pharmacist@example.com")
                .setPasswordHash("encoded")
                .setRole(User.UserRole.PHARMACIST)
                .build();

        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token, user));
        assertEquals("pharmacist@example.com", jwtService.extractClaims(token).getSubject());
        assertEquals("PHARMACIST", jwtService.extractClaims(token).get("role"));
    }

    @Test
    void tokenCannotBeUsedForAnotherUser() {
        User owner = new User.Builder()
                .setId(10L)
                .setFullName("Owner")
                .setEmail("owner@example.com")
                .setPasswordHash("encoded")
                .setRole(User.UserRole.CUSTOMER)
                .build();
        User other = new User.Builder()
                .setId(11L)
                .setFullName("Other")
                .setEmail("other@example.com")
                .setPasswordHash("encoded")
                .setRole(User.UserRole.CUSTOMER)
                .build();

        assertFalse(jwtService.isTokenValid(jwtService.generateToken(owner), other));
    }
}
