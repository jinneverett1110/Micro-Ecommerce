// PaymentRepository.java
package quant.paymentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import quant.paymentservice.entity.Payment;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByOrderId(String orderId);
    boolean existsByOrderId(String orderId);
}