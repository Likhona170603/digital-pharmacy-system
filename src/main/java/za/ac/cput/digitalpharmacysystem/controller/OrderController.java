package za.ac.cput.digitalpharmacysystem.controller;
/*
 * Sibulele Pama
 * 221447598
 */

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import za.ac.cput.digitalpharmacysystem.domain.Order;
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
    public ResponseEntity<Order> createOrder(@RequestBody PlaceOrderRequest request) {
        return new ResponseEntity<>(service.placeOrder(request), HttpStatus.CREATED);
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<Order> readOrder(@PathVariable String id) {
        Order order = service.read(id);
        if (order == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @GetMapping("/getall")
    public ResponseEntity<List<Order>> getAll() {
        return new ResponseEntity<>(service.getAll(), HttpStatus.OK);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getByCustomer(@PathVariable String customerId) {
        return new ResponseEntity<>(service.getByCustomer(customerId), HttpStatus.OK);
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<Order> pay(@PathVariable String id) {
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
    public ResponseEntity<Order> cancelOrder(@PathVariable String id) {
        return new ResponseEntity<>(service.cancelOrder(id), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        if (!service.delete(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}