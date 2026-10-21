package za.ac.cput.digitalpharmacysystem.repository;
/*
 * Sibulele Pama
 * 221447598
 */

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.cput.digitalpharmacysystem.domain.OrderItem;

import java.util.List;


@Repository
public interface IOrderItemRepository extends JpaRepository<OrderItem, String> {

    List<OrderItem> findByOrderId(String orderId);

    List<OrderItem> findByOrderIdAndProductId(String orderId, String productId);
}
