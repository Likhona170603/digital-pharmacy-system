package za.ac.cput.digitalpharmacysystem.controller;
/*
 * Likhona Noroyita
 * 230154239
 */

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import za.ac.cput.digitalpharmacysystem.domain.Prescription;
import za.ac.cput.digitalpharmacysystem.domain.User;
import za.ac.cput.digitalpharmacysystem.factory.PrescriptionFactory;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PrescriptionControllerTest {

    @Autowired
    private PrescriptionController controller;

    @Test
    void create() {

        Prescription prescription =
                PrescriptionFactory.createPrescription(
                        "PAT301",
                        "prescriptions/test.pdf",
                        LocalDate.now()
                );

        User user = new User.Builder()
                .setId(301L)
                .setFullName("Test Customer")
                .setEmail("test.customer@example.com")
                .setPasswordHash("unused")
                .setRole(User.UserRole.CUSTOMER)
                .build();
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                user, null, List.of());
        var response = controller.create(prescription, authentication);

        assertNotNull(response);
        assertNotNull(response.getBody());

        assertEquals(
                201,
                response.getStatusCode().value()
        );
    }

    @Test
    void getAll() {

        var response =
                controller.getAll();

        assertNotNull(response);
        assertNotNull(response.getBody());

        assertEquals(
                200,
                response.getStatusCode().value()
        );
    }
}