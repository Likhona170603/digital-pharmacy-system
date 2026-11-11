package za.ac.cput.digitalpharmacysystem.dto;

import za.ac.cput.digitalpharmacysystem.domain.User;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        User.UserRole role,
        boolean active
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getPhone(), user.getRole(), user.isActive());
    }
}
