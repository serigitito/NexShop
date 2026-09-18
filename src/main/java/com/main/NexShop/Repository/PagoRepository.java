package com.main.NexShop.Repository;

import com.main.NexShop.Model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findAllByOrderByIdPagoDesc();
}
