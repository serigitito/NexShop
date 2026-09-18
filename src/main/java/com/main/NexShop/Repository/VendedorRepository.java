package com.main.NexShop.Repository;

import com.main.NexShop.Model.Vendedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendedorRepository extends JpaRepository<Vendedor, Long> {
    Vendedor findByNoDocumento(Long noDocumento);
}
