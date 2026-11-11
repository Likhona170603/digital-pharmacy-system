package za.ac.cput.digitalpharmacysystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.digitalpharmacysystem.domain.PatientProfile;
import za.ac.cput.digitalpharmacysystem.domain.User;
import za.ac.cput.digitalpharmacysystem.service.IPatientProfileService;
import za.ac.cput.digitalpharmacysystem.repository.UserRepository;

import java.util.List;

//Author: 221066748 (Noxolo Portia Ngubo)

@RestController
@RequestMapping("/digitalpharmacysystem/patientprofile")
public class PatientProfileController {

    private final IPatientProfileService profileService;
    private final UserRepository userRepository;

    @Autowired
    public PatientProfileController(IPatientProfileService profileService, UserRepository userRepository) {
        this.profileService = profileService;
        this.userRepository = userRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<PatientProfile> create(@RequestBody PatientProfile profile,
                                                  Authentication authentication) {
        User user = currentUser(authentication);
        if (user == null || user.getRole() != User.UserRole.CUSTOMER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        PatientProfile ownedProfile = new PatientProfile.Builder()
                .copy(profile)
                .setId(null)
                .setUser(userRepository.findById(user.getId()).orElseThrow())
                .build();
        PatientProfile created = profileService.create(ownedProfile);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<PatientProfile> read(@PathVariable Long id, Authentication authentication) {
        PatientProfile profile = profileService.read(id);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        if (!isAdmin(authentication) && !owns(authentication, profile)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/update")
    public ResponseEntity<PatientProfile> update(@RequestBody PatientProfile profile,
                                                  Authentication authentication) {
        PatientProfile existing = profile.getId() == null ? null : profileService.read(profile.getId());
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        if (!isAdmin(authentication) && !owns(authentication, existing)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        PatientProfile updatedProfile = new PatientProfile.Builder()
                .copy(existing)
                .setDateOfBirth(profile.getDateOfBirth())
                .setAllergies(profile.getAllergies())
                .setChronicConditions(profile.getChronicConditions())
                .setEmergencyContactName(profile.getEmergencyContactName())
                .setEmergencyContactPhone(profile.getEmergencyContactPhone())
                .build();
        PatientProfile updated = profileService.update(updatedProfile);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        PatientProfile profile = profileService.read(id);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        if (!isAdmin(authentication) && !owns(authentication, profile)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        boolean deleted = profileService.delete(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<PatientProfile>> getAll() {
        return ResponseEntity.ok(profileService.getAll());
    }

    private User currentUser(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof User user ? user : null;
    }

    private boolean isAdmin(Authentication authentication) {
        User user = currentUser(authentication);
        return user != null && user.getRole() == User.UserRole.ADMIN;
    }

    private boolean owns(Authentication authentication, PatientProfile profile) {
        User user = currentUser(authentication);
        return user != null && profile.getUser() != null
                && user.getId().equals(profile.getUser().getId());
    }
}
