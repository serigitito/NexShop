package com.main.NexShop.Repository;

import com.main.NexShop.Model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findAllByOrderByIdProductoDesc();
    List<Producto> findByNombreContainingIgnoreCaseOrderByIdProductoDesc(String nombre);
    List<Producto> findByCategoriaOrderByIdProductoDesc(String categoria);
    List<Producto> findByCategoria(String categoria);
    List<Producto> findByIdVendedorOrderByIdProductoDesc(Long idVendedor);
}
