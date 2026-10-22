package za.ac.cput.digitalpharmacysystem.controller;
/*
 * Sibulele Pama
 * 221447598
 */

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import za.ac.cput.digitalpharmacysystem.domain.Order;
import za.ac.cput.digitalpharmacysystem.repository.IOrderItemRepository;
import za.ac.cput.digitalpharmacysystem.repository.OrderRepository;

@RestController
@RequestMapping("/digitalpharmacysystem/order")
public class OrderController {
    private final OrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;

    public OrderController(OrderRepository orderRepository, IOrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        Order createdOrder = orderRepository.save(order);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);

    }

    @GetMapping("/read/{id}")
    public ResponseEntity<Order> readOrder(@PathVariable String id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<Order> updateOrder(@RequestBody Order order) {
        Order updatedOrder = orderRepository.save(order);
        return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        orderRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
