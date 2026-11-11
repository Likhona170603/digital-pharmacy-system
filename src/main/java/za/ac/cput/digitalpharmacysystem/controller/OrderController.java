package za.ac.cput.digitalpharmacysystem.controller;
/*
 * Sibulele Pama
 * 221447598
 */

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import za.ac.cput.digitalpharmacysystem.domain.Order;
import za.ac.cput.digitalpharmacysystem.domain.User;
import za.ac.cput.digitalpharmacysystem.dto.PlaceOrderRequest;
import za.ac.cput.digitalpharmacysystem.service.IOrderService;

import java.util.List;


@RestController
@RequestMapping("/digitalpharmacysystem/order")
public class OrderController {
    private final IOrderService service;

    public OrderController(IOrderService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(@RequestBody PlaceOrderRequest request,
                                             Authentication authentication) {
        request.setCustomerId(String.valueOf(currentUser(authentication).getId()));
        return new ResponseEntity<>(service.placeOrder(request), HttpStatus.CREATED);
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<Order> readOrder(@PathVariable String id, Authentication authentication) {
        Order order = service.read(id);
        if (order == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!canViewAll(authentication) && !owns(authentication, order.getCustomerId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @GetMapping("/getall")
    public ResponseEntity<List<Order>> getAll() {
        return new ResponseEntity<>(service.getAll(), HttpStatus.OK);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getByCustomer(@PathVariable String customerId,
                                                      Authentication authentication) {
        if (!canViewAll(authentication) && !owns(authentication, customerId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(service.getByCustomer(customerId), HttpStatus.OK);
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<Order> pay(@PathVariable String id, Authentication authentication) {
        if (!canManageCustomerOrder(authentication, id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(service.pay(id), HttpStatus.OK);
    }

    @PostMapping("/{id}/pack")
    public ResponseEntity<Order> pack(@PathVariable String id) {
        return new ResponseEntity<>(service.pack(id), HttpStatus.OK);
    }

    @PostMapping("/{id}/collect")
    public ResponseEntity<Order> collect(@PathVariable String id) {
        return new ResponseEntity<>(service.collect(id), HttpStatus.OK);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable String id, Authentication authentication) {
        if (!canManageCustomerOrder(authentication, id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(service.cancelOrder(id), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        if (!service.delete(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private User currentUser(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof User user ? user : null;
    }

    private boolean owns(Authentication authentication, String customerId) {
        User user = currentUser(authentication);
        return user != null && String.valueOf(user.getId()).equals(customerId);
    }

    private boolean canViewAll(Authentication authentication) {
        User user = currentUser(authentication);
        return user != null && (user.getRole() == User.UserRole.ADMIN
                || user.getRole() == User.UserRole.PHARMACIST);
    }

    private boolean canManageCustomerOrder(Authentication authentication, String orderId) {
        Order order = service.read(orderId);
        User user = currentUser(authentication);
        return order != null && user != null && (user.getRole() == User.UserRole.ADMIN
                || (user.getRole() == User.UserRole.CUSTOMER && owns(authentication, order.getCustomerId())));
    }
}