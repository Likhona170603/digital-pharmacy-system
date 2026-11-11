package za.ac.cput.digitalpharmacysystem.dto;

import za.ac.cput.digitalpharmacysystem.domain.User;

public record CreateStaffUserRequest(
        String fullName,
        String email,
        String password,
        String phone,
        User.UserRole role
) {
}
