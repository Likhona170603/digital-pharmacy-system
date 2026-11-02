package za.ac.cput.digitalpharmacysystem.dto;

public record LoginResponse(
        String token,
        String tokenType,
        UserResponse user
) {
}

