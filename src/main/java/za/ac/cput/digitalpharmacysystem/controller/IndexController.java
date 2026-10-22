package za.ac.cput.digitalpharmacysystem.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * Sesona Ntshatsha
 * 240773365

 */

@RestController
@RequestMapping("/")
public class IndexController {
    @GetMapping
    public ResponseEntity<String> home() {
        return new ResponseEntity<>("Welcome to Digital Pharmacy System API", HttpStatus.OK);
    }
}

