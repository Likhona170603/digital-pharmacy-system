package za.ac.cput.digitalpharmacysystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.digitalpharmacysystem.domain.Delivery;
import za.ac.cput.digitalpharmacysystem.domain.DeliveryStatus;
import za.ac.cput.digitalpharmacysystem.domain.User;
import za.ac.cput.digitalpharmacysystem.service.IDeliveryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


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
    public ResponseEntity<Delivery> read(@PathVariable String id, Authentication authentication) {
        Delivery delivery = service.read(id);
        if (delivery == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!canAccessDelivery(authentication, delivery)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
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



    @PatchMapping("/{id}/status")
    public ResponseEntity<Delivery> updateStatus(
            @PathVariable String id,
            @RequestParam DeliveryStatus status,
            @RequestParam(required = false) String recipientName,
            Authentication authentication) {
        Delivery delivery = service.read(id);
        if (delivery == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        User user = currentUser(authentication);
        if (user == null || (user.getRole() != User.UserRole.ADMIN
                && (delivery.getCourier() == null || !user.getId().equals(delivery.getCourier().getId())))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(service.updateStatus(id, status, recipientName), HttpStatus.OK);
    }

    @PutMapping("/{id}/courier/{courierId}")
    public ResponseEntity<Delivery> assignCourier(
            @PathVariable String id,
            @PathVariable Long courierId) {

        return new ResponseEntity<>(service.assignCourier(id, courierId), HttpStatus.OK);
    }

    @GetMapping("/order/{orderNumber}")
    public ResponseEntity<Delivery> getByOrderNumber(
            @PathVariable String orderNumber,
            Authentication authentication) {

        Delivery delivery = service.findByOrderNumber(orderNumber);

        if (delivery == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        User user = currentUser(authentication);
        boolean customerOwner = user != null && user.getRole() == User.UserRole.CUSTOMER
                && delivery.getOrder() != null
                && String.valueOf(user.getId()).equals(delivery.getOrder().getCustomerId());
        boolean assignedCourier = user != null && user.getRole() == User.UserRole.COURIER
                && delivery.getCourier() != null && user.getId().equals(delivery.getCourier().getId());
        if (!customerOwner && !assignedCourier && !isStaff(authentication)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(delivery, HttpStatus.OK);
    }

    @GetMapping("/courier/{courierId}/manifest")
    public ResponseEntity<List<Delivery>> getCourierManifest(
            @PathVariable Long courierId,
            Authentication authentication) {
        User user = currentUser(authentication);
        if (user == null || (user.getRole() != User.UserRole.ADMIN && !user.getId().equals(courierId))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<Delivery> deliveries = service.getCourierManifest(courierId);

        return new ResponseEntity<>(deliveries, HttpStatus.OK);
    }

    private User currentUser(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof User user ? user : null;
    }

    private boolean isStaff(Authentication authentication) {
        User user = currentUser(authentication);
        return user != null && (user.getRole() == User.UserRole.ADMIN
                || user.getRole() == User.UserRole.PHARMACIST);
    }

    private boolean canAccessDelivery(Authentication authentication, Delivery delivery) {
        User user = currentUser(authentication);
        if (user == null) {
            return false;
        }
        if (user.getRole() == User.UserRole.ADMIN || user.getRole() == User.UserRole.PHARMACIST) {
            return true;
        }
        if (user.getRole() == User.UserRole.COURIER) {
            return delivery.getCourier() != null && user.getId().equals(delivery.getCourier().getId());
        }
        return user.getRole() == User.UserRole.CUSTOMER && delivery.getOrder() != null
                && String.valueOf(user.getId()).equals(delivery.getOrder().getCustomerId());
    }
}