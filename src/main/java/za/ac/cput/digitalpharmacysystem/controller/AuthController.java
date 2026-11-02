package za.ac.cput.digitalpharmacysystem.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.ac.cput.digitalpharmacysystem.dto.RegisterRequest;
import za.ac.cput.digitalpharmacysystem.dto.UserResponse;
import za.ac.cput.digitalpharmacysystem.service.AuthService;
import za.ac.cput.digitalpharmacysystem.dto.LoginRequest;
import za.ac.cput.digitalpharmacysystem.dto.LoginResponse;

import java.util.Map;

@RestController
@RequestMapping("/digitalpharmacysystem/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            UserResponse created = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (AuthService.EmailAlreadyRegisteredException | DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "An account with this email already exists."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (AuthService.InvalidCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Invalid email or password."
                    ));
        }
    }

}
