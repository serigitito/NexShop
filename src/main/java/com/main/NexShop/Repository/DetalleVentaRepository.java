package com.main.NexShop.Repository;

import com.main.NexShop.Model.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
    List<DetalleVenta> findByIdVenta(Long idVenta);
    void deleteByIdVenta(Long idVenta);
    long countByIdProducto(Long idProducto);
}
