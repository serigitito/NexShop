package com.main.NexShop.Repository;

import com.main.NexShop.Model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Cliente findByNoDocumento(Long noDocumento);
}
