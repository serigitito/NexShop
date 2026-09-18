package com.main.NexShop.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Vendedor")
public class Vendedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idVendedor")
    private Long idVendedor;

    @Column(name = "NoDocumento")
    private Long noDocumento;

    public Long getIdVendedor() { return idVendedor; }
    public void setIdVendedor(Long idVendedor) { this.idVendedor = idVendedor; }

    public Long getNoDocumento() { return noDocumento; }
    public void setNoDocumento(Long noDocumento) { this.noDocumento = noDocumento; }
}
