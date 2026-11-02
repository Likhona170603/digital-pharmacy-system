package za.ac.cput.digitalpharmacysystem.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import za.ac.cput.digitalpharmacysystem.domain.User;
import za.ac.cput.digitalpharmacysystem.repository.UserRepository;

@Configuration
public class UserDataSeeder {

    @Bean
    CommandLineRunner seedUsers(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            // Seed Admin
            createUserIfMissing(
                    userRepository,
                    passwordEncoder,
                    "PharmaFlow Admin",
                    "admin@pharmaflow.local",
                    "Admin123!",
                    User.UserRole.ADMIN
            );

            createUserIfMissing(
                    userRepository,
                    passwordEncoder,
                    "PharmaFlow Courier",
                    "courier@pharmaflow.local",
                    "Courier123!",
                    User.UserRole.COURIER
            );

            createUserIfMissing(
                    userRepository,
                    passwordEncoder,
                    "PharmaFlow Pharmacist",
                    "pharmacist@pharmaflow.local",
                    "Pharmacist123!",
                    User.UserRole.PHARMACIST
            );
        };
    }


    private void createUserIfMissing(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String fullName,
            String email,
            String rawPassword,
            User.UserRole role) {

        if (userRepository.existsByEmail(email)) {
            System.out.println("Seed user already exists: " + email);
            return;
        }

        User user = new User.Builder()
                .setFullName(fullName)
                .setEmail(email)
                .setPasswordHash(passwordEncoder.encode(rawPassword))
                .setRole(role)
                .build();

        userRepository.save(user);

        System.out.println(
                "Created seed user: " + email + " (" + role + ")");
    }
}