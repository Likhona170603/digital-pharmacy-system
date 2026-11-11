package za.ac.cput.digitalpharmacysystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.digitalpharmacysystem.dto.CustomerNameDto;
import za.ac.cput.digitalpharmacysystem.dto.CreateStaffUserRequest;
import za.ac.cput.digitalpharmacysystem.domain.User;
import za.ac.cput.digitalpharmacysystem.repository.UserRepository;

import java.util.List;
import java.util.Locale;

//Author: 221066748 (Noxolo Portia Ngubo)

@Service
public class UserService implements IUserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User create(User user) {
        return repository.save(user);
    }

    @Override
    public User read(Long id) {
        return repository.findById(id).orElse(null);
    }


    @Override
    public User update(User user) {
        if (user == null || user.getId() == null) {
            return null;
        }

        User existing = repository.findById(user.getId()).orElse(null);

        if (existing == null) {
            return null;
        }

        User updated = new User.Builder()
                .copy(existing)
                .setFullName(user.getFullName())
                .setPhone(user.getPhone())
                .build();

        return repository.save(updated);
    }


    @Override
    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<User> getAll() {
        return repository.findAll();
    }

    @Override
    public List<CustomerNameDto> getCustomerNames() {
        return repository.findNamesByRole(User.UserRole.CUSTOMER);
    }

    @Override
    @Transactional
    public User createStaffUser(CreateStaffUserRequest request) {
        if (request == null || blank(request.fullName()) || blank(request.email())
                || blank(request.password()) || request.role() == null) {
            throw new IllegalArgumentException("Name, email, password and staff role are required.");
        }
        String fullName = request.fullName().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (fullName.length() > 120 || email.length() > 190
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Enter a valid name and email address.");
        }
        if (request.password().length() < 8 || request.password().length() > 72) {
            throw new IllegalArgumentException("Password must contain between 8 and 72 characters.");
        }
        if (request.role() != User.UserRole.PHARMACIST
                && request.role() != User.UserRole.COURIER) {
            throw new IllegalArgumentException("Only pharmacist or courier accounts can be created here.");
        }
        if (repository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        User user = new User.Builder()
                .setFullName(fullName)
                .setEmail(email)
                .setPhone(blank(request.phone()) ? null : request.phone().trim())
                .setPasswordHash(passwordEncoder.encode(request.password()))
                .setRole(request.role())
                .setActive(true)
                .build();
        return repository.save(user);
    }

    @Override
    @Transactional
    public User setActive(Long id, boolean active) {
        User existing = repository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }
        if (!active && existing.isActive() && existing.getRole() == User.UserRole.ADMIN
                && repository.countByRoleAndActiveTrue(User.UserRole.ADMIN) <= 1) {
            throw new IllegalStateException("The last active administrator cannot be suspended.");
        }
        User updated = new User.Builder()
                .copy(existing)
                .setActive(active)
                .build();
        return repository.save(updated);
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}