package za.ac.cput.digitalpharmacysystem.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import za.ac.cput.digitalpharmacysystem.domain.User;
import za.ac.cput.digitalpharmacysystem.dto.CreateStaffUserRequest;
import za.ac.cput.digitalpharmacysystem.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(repository, passwordEncoder);
    }

    @Test
    void createsActiveStaffAccountWithEncodedPassword() {
        when(repository.existsByEmail("staff@example.com")).thenReturn(false);
        when(passwordEncoder.encode("StrongPass123!")).thenReturn("encoded-password");
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = service.createStaffUser(new CreateStaffUserRequest(
                " Staff User ", "STAFF@example.com", "StrongPass123!", "12345",
                User.UserRole.PHARMACIST));

        assertEquals("Staff User", created.getFullName());
        assertEquals("staff@example.com", created.getEmail());
        assertEquals("encoded-password", created.getPasswordHash());
        assertEquals(User.UserRole.PHARMACIST, created.getRole());
        assertTrue(created.isActive());
        verify(repository).save(created);
    }

    @Test
    void rejectsNonStaffRoles() {
        assertThrows(IllegalArgumentException.class, () -> service.createStaffUser(
                new CreateStaffUserRequest("Admin", "admin@example.com", "StrongPass123!",
                        null, User.UserRole.ADMIN)));
        verify(repository, never()).save(any());
    }

    @Test
    void preventsSuspendingTheLastActiveAdministrator() {
        User admin = user(1L, User.UserRole.ADMIN, true);
        when(repository.findById(1L)).thenReturn(Optional.of(admin));
        when(repository.countByRoleAndActiveTrue(User.UserRole.ADMIN)).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> service.setActive(1L, false));
        verify(repository, never()).save(any());
    }

    @Test
    void canSuspendRegularUser() {
        User customer = user(2L, User.UserRole.CUSTOMER, true);
        when(repository.findById(2L)).thenReturn(Optional.of(customer));
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User suspended = service.setActive(2L, false);

        assertFalse(suspended.isActive());
        assertEquals(customer.getRole(), suspended.getRole());
    }

    private User user(Long id, User.UserRole role, boolean active) {
        return new User.Builder()
                .setId(id)
                .setFullName("Test User")
                .setEmail("test@example.com")
                .setPasswordHash("encoded")
                .setRole(role)
                .setActive(active)
                .build();
    }
}
