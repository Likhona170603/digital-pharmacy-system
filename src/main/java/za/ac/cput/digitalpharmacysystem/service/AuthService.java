package za.ac.cput.digitalpharmacysystem.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.digitalpharmacysystem.domain.User;
import za.ac.cput.digitalpharmacysystem.dto.LoginRequest;
import za.ac.cput.digitalpharmacysystem.dto.LoginResponse;
import za.ac.cput.digitalpharmacysystem.dto.RegisterRequest;
import za.ac.cput.digitalpharmacysystem.dto.UserResponse;
import za.ac.cput.digitalpharmacysystem.repository.UserRepository;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (request == null || blank(request.fullName())
                || blank(request.email()) || blank(request.password())) {
            throw new IllegalArgumentException(
                    "Full name, email and password are required.");
        }

        String name = request.fullName().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String password = request.password();

        if (name.length() > 120) {
            throw new IllegalArgumentException(
                    "Full name must be 120 characters or fewer.");
        }

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException(
                    "Enter a valid email address.");
        }

        if (email.length() > 190) {
            throw new IllegalArgumentException(
                    "Email address is too long.");
        }

        if (password.length() < 8 || password.length() > 72) {
            throw new IllegalArgumentException(
                    "Password must contain between 8 and 72 characters.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }

        User user = new User.Builder()
                .setFullName(name)
                .setEmail(email)
                .setPhone(blank(request.phone()) ? null : request.phone().trim())
                .setPasswordHash(passwordEncoder.encode(password))
                .setRole(User.UserRole.CUSTOMER)
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        if (request == null || blank(request.email())
                || blank(request.password())) {
            throw new InvalidCredentialsException();
        }

        String email = request.email().trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(
                request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                token,
                "Bearer",
                UserResponse.from(user)
        );
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static class EmailAlreadyRegisteredException
            extends RuntimeException {
    }

    public static class InvalidCredentialsException
            extends RuntimeException {
    }
}