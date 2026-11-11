package za.ac.cput.digitalpharmacysystem.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import za.ac.cput.digitalpharmacysystem.domain.User;
import za.ac.cput.digitalpharmacysystem.dto.LoginRequest;
import za.ac.cput.digitalpharmacysystem.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void suspendedUserCannotLogInEvenWithCorrectPassword() {
        User suspended = new User.Builder()
                .setId(8L)
                .setFullName("Suspended User")
                .setEmail("suspended@example.com")
                .setPasswordHash("encoded")
                .setRole(User.UserRole.CUSTOMER)
                .setActive(false)
                .build();
        when(userRepository.findByEmail("suspended@example.com")).thenReturn(Optional.of(suspended));
        JwtService jwtService = new JwtService(
                "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=", 3_600_000);
        AuthService service = new AuthService(
                userRepository, new BCryptPasswordEncoder(), jwtService);

        assertThrows(AuthService.InvalidCredentialsException.class,
                () -> service.login(new LoginRequest("suspended@example.com", "password123")));
    }
}
