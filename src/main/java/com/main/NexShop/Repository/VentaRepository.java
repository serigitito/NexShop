package com.main.NexShop.Repository;

import com.main.NexShop.Model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    long countByEstado(String estado);
    List<Venta> findAllByOrderByIdVentaDesc();
    List<Venta> findByIdVendedorOrderByFechaDesc(Long idVendedor);
    List<Venta> findByEstadoOrderByIdVentaDesc(String estado);
}
