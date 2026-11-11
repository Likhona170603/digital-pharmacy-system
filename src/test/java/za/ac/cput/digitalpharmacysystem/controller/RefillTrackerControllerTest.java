package za.ac.cput.digitalpharmacysystem.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import za.ac.cput.digitalpharmacysystem.domain.RefillTracker;
import za.ac.cput.digitalpharmacysystem.domain.User;
import za.ac.cput.digitalpharmacysystem.factory.RefillTrackerFactory;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RefillTrackerControllerTest {

    @Autowired
    private RefillTrackerController controller;

    @Test
    void create() {

        RefillTracker tracker =
                RefillTrackerFactory.createRefillTracker(
                        "RX301",
                        6,
                        LocalDate.now()
                );

        var response =
                controller.create(tracker);

        assertNotNull(response);
        assertNotNull(response.getBody());

        assertEquals(
                201,
                response.getStatusCode().value()
        );
    }

    @Test
    void read() {

        RefillTracker tracker =
                RefillTrackerFactory.createRefillTracker(
                        "RX302",
                        6,
                        LocalDate.now()
                );

        var created =
                controller.create(tracker);

        assertNotNull(created);
        assertNotNull(created.getBody());

        String id =
                created.getBody()
                        .getRefillTrackerId();

        User user = new User.Builder()
                .setId(301L)
                .setFullName("Test Admin")
                .setEmail("test.admin@example.com")
                .setPasswordHash("unused")
                .setRole(User.UserRole.ADMIN)
                .build();
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                user, null, List.of());
        var response = controller.read(id, authentication);

        assertNotNull(response);
        assertNotNull(response.getBody());

        assertEquals(
                200,
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