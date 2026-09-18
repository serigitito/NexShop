package com.main.NexShop.Service;

import com.main.NexShop.Model.Venta;
import com.main.NexShop.Repository.ProductoRepository;
import com.main.NexShop.Repository.UsuarioRepository;
import com.main.NexShop.Repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private VentaRepository ventaRepository;
    @Autowired private VentaService ventaService;

    public Map<String, Object> estadisticas() {
        Map<String, Object> datos = new HashMap<>();

        datos.put("totalUsuarios", usuarioRepository.count());
        datos.put("totalProductos", productoRepository.count());
        datos.put("totalVentas", ventaRepository.countByEstado("Pagada"));

        double ingresos = 0;

        for (Venta venta : ventaRepository.findByEstadoOrderByIdVentaDesc("Pagada")) {
            ingresos += venta.getCostoFinal();
        }

        datos.put("ingresos", ingresos);
        return datos;
    }

    public List<Venta> ventasRecientes(int limite) {
        List<Venta> resultado = new ArrayList<>();

        for (Venta venta : ventaRepository.findAllByOrderByIdVentaDesc()) {
            resultado.add(venta);
            if (resultado.size() == limite) {
                break;
            }
        }

        return resultado;
    }

    public List<Venta> listarVentas(String texto) {
        List<Venta> ventas = ventaRepository.findAllByOrderByIdVentaDesc();

        if (texto == null || texto.isBlank()) {
            return ventas;
        }

        List<Venta> resultado = new ArrayList<>();
        String buscar = texto.toLowerCase();

        for (Venta venta : ventas) {
            String cliente = ventaService.nombreCliente(venta.getIdCliente()).toLowerCase();
            String vendedor = ventaService.nombreVendedor(venta.getIdVendedor()).toLowerCase();
            String id = String.valueOf(venta.getIdVenta());

            if (cliente.contains(buscar) || vendedor.contains(buscar) || id.contains(buscar)) {
                resultado.add(venta);
            }
        }

        return resultado;
    }
}
