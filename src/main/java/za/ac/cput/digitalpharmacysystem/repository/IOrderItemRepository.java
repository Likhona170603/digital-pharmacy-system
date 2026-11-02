package za.ac.cput.digitalpharmacysystem.repository;
/*
 * Sibulele Pama
 * 221447498
 */

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.cput.digitalpharmacysystem.domain.OrderItem;

import java.util.List;


@Repository
public interface IOrderItemRepository extends JpaRepository<OrderItem, String> {

    List<OrderItem> findByOrder_OrderNumber(String orderNumber);

    List<OrderItem> findByProduct_ProductId(String productId);

}
