package za.ac.cput.digitalpharmacysystem.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import za.ac.cput.digitalpharmacysystem.domain.Prescription;
import za.ac.cput.digitalpharmacysystem.factory.PrescriptionFactory;

import java.time.LocalDate;

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

        var response =
                controller.create(prescription);

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