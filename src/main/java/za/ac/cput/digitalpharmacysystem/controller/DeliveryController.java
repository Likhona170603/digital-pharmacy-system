package za.ac.cput.digitalpharmacysystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.digitalpharmacysystem.domain.Delivery;
import za.ac.cput.digitalpharmacysystem.service.IDeliveryService;

import java.util.List;

/*
 * Sesona Ntshatsha
 * 240773365

 */

@RestController
@RequestMapping("/digitalpharmacysystem/delivery")
public class DeliveryController {
    private final IDeliveryService service;

    @Autowired
    public DeliveryController(IDeliveryService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<Delivery> create(@RequestBody Delivery delivery) {
        Delivery created = service.create(delivery);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<Delivery> read(@PathVariable String id) {
        Delivery delivery = service.read(id);
        if (delivery == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(delivery, HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<Delivery> update(@RequestBody Delivery delivery) {
        Delivery updated = service.update(delivery);
        if (updated == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean deleted = service.delete(id);
        if (!deleted) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/getall")
    public ResponseEntity<List<Delivery>> getAll() {
        List<Delivery> deliveries = service.getAll();
        return new ResponseEntity<>(deliveries, HttpStatus.OK);
    }
}