package za.ac.cput.digitalpharmacysystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.digitalpharmacysystem.dto.CustomerNameDto;
import za.ac.cput.digitalpharmacysystem.dto.CreateStaffUserRequest;
import za.ac.cput.digitalpharmacysystem.domain.User;
import za.ac.cput.digitalpharmacysystem.service.IUserService;

import java.util.List;
import java.util.Map;

//Author: 221066748 (Noxolo Portia Ngubo)

@RestController
@RequestMapping("/digitalpharmacysystem/user")
public class UserController {

    private final IUserService userService;

    @Autowired
    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<User> create(@RequestBody User user) {
        User created = userService.createStaffUser(new CreateStaffUserRequest(
                user.getFullName(), user.getEmail(), user.getPasswordHash(), user.getPhone(), user.getRole()));
        return ResponseEntity.ok(created);
    }

    @PostMapping("/staff")
    public ResponseEntity<?> createStaff(@RequestBody CreateStaffUserRequest request) {
        try {
            return ResponseEntity.status(201).body(userService.createStaffUser(request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<User> read(@PathVariable Long id, Authentication authentication) {
        User user = userService.read(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        if (!isAdmin(authentication) && !isCurrentUser(authentication, id)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("/update")
    public ResponseEntity<User> update(@RequestBody User user, Authentication authentication) {
        if (user.getId() == null || (!isAdmin(authentication)
                && !isCurrentUser(authentication, user.getId()))) {
            return ResponseEntity.status(403).build();
        }
        User updated = userService.update(user);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = userService.delete(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<?> setActive(
            @PathVariable Long id,
            @RequestParam boolean active,
            Authentication authentication) {
        if (isCurrentUser(authentication, id)) {
            return ResponseEntity.badRequest().body(Map.of("message", "You cannot suspend your own account."));
        }
        try {
            User updated = userService.setActive(id, active);
            return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/names")
    public ResponseEntity<List<CustomerNameDto>> getCustomerNames() {
        return ResponseEntity.ok(userService.getCustomerNames());
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof User user
                && user.getRole() == User.UserRole.ADMIN;
    }

    private boolean isCurrentUser(Authentication authentication, Long id) {
        return authentication != null && authentication.getPrincipal() instanceof User user
                && user.getId().equals(id);
    }
}
