package com.main.NexShop.Service;

import com.main.NexShop.Model.Cliente;
import com.main.NexShop.Model.DetalleVenta;
import com.main.NexShop.Model.Pago;
import com.main.NexShop.Model.Producto;
import com.main.NexShop.Model.Usuario;
import com.main.NexShop.Model.Vendedor;
import com.main.NexShop.Model.Venta;
import com.main.NexShop.Repository.ClienteRepository;
import com.main.NexShop.Repository.DetalleVentaRepository;
import com.main.NexShop.Repository.PagoRepository;
import com.main.NexShop.Repository.ProductoRepository;
import com.main.NexShop.Repository.UsuarioRepository;
import com.main.NexShop.Repository.VendedorRepository;
import com.main.NexShop.Repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class VentaService {

    @Autowired private VentaRepository ventaRepository;
    @Autowired private DetalleVentaRepository detalleRepository;
    @Autowired private PagoRepository pagoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private VendedorRepository vendedorRepository;
    @Autowired private ProductoRepository productoRepository;

    public Usuario obtenerUsuario(String usuario) {
        return usuarioRepository.findByUsuario(usuario);
    }

    public Cliente obtenerClientePorDocumento(Long documento) {
        return clienteRepository.findByNoDocumento(documento);
    }

    public Vendedor obtenerVendedor(Long documento) {
        return vendedorRepository.findByNoDocumento(documento);
    }

    public Producto obtenerProducto(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    public Venta crearVenta(String metodoPago, Long idCliente, Long idVendedor, String direccion) {
        Venta venta = new Venta();
        venta.setFecha(LocalDate.now());
        venta.setMetodoPago(metodoPago);
        venta.setSubtotal(0.0);
        venta.setIva(0.0);
        venta.setCostoFinal(0.0);
        venta.setIdCliente(idCliente);
        venta.setIdVendedor(idVendedor);
        venta.setDireccionEnvio(direccion);
        venta.setEstado("Pendiente");
        venta.setEstadoEnvio("Preparando");
        return ventaRepository.save(venta);
    }

    public void agregarDetalle(Long idVenta, Long idProducto, Integer cantidad, Double precio) {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setIdVenta(idVenta);
        detalle.setIdProducto(idProducto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(precio);
        detalle.setSubtotal(precio * cantidad);
        detalleRepository.save(detalle);
    }

    public double actualizarTotales(Long idVenta) {
        double subtotal = 0;

        for (DetalleVenta detalle : detalleRepository.findByIdVenta(idVenta)) {
            subtotal += detalle.getSubtotal();
        }

        double iva = subtotal * 0.19;
        double total = subtotal + iva;
        Venta venta = obtenerVenta(idVenta);

        if (venta != null) {
            venta.setSubtotal(subtotal);
            venta.setIva(iva);
            venta.setCostoFinal(total);
            venta.setEstado("Pagada");
            ventaRepository.save(venta);
        }

        return total;
    }

    public void registrarPago(Long idVenta, Double iva, String metodo, String estado, String historial) {
        Pago pago = new Pago();
        pago.setIdVenta(idVenta);
        pago.setIva(iva);
        pago.setMetodoPago(metodo);
        pago.setEstado(estado);
        pago.setHistorial(historial);
        pagoRepository.save(pago);
    }

    public List<Venta> listarVentasVendedor(Long idVendedor, String buscar) {
        List<Venta> ventas = ventaRepository.findByIdVendedorOrderByFechaDesc(idVendedor);

        if (buscar == null || buscar.isBlank()) {
            return ventas;
        }

        List<Venta> resultado = new ArrayList<>();

        for (Venta venta : ventas) {
            if (String.valueOf(venta.getIdVenta()).contains(buscar)) {
                resultado.add(venta);
            }
        }

        return resultado;
    }

    public List<Venta> listarTodas() {
        return ventaRepository.findAllByOrderByIdVentaDesc();
    }

    public List<Pago> listarPagos() {
        return pagoRepository.findAllByOrderByIdPagoDesc();
    }

    public Venta obtenerVenta(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }

    public List<DetalleVenta> obtenerDetalles(Long idVenta) {
        return detalleRepository.findByIdVenta(idVenta);
    }

    public void actualizarVenta(Long id, String estado, String metodoPago, String direccion) {
        Venta venta = obtenerVenta(id);

        if (venta == null) {
            return;
        }

        venta.setEstado(estado);
        venta.setMetodoPago(metodoPago);
        venta.setDireccionEnvio(direccion);
        ventaRepository.save(venta);
    }

    public void actualizarVentaEnvio(Venta venta) {
        ventaRepository.save(venta);
    }

    public void eliminarVenta(Long id) {
        detalleRepository.deleteByIdVenta(id);
        ventaRepository.deleteById(id);
    }

    public String nombreCliente(Long idCliente) {
        Cliente cliente = clienteRepository.findById(idCliente).orElse(null);

        if (cliente == null) {
            return "Sin cliente";
        }

        Usuario usuario = usuarioRepository.findById(cliente.getNoDocumento()).orElse(null);
        return usuario == null ? "Sin cliente" : usuario.getNombre();
    }

    public String correoCliente(Long idCliente) {
        Cliente cliente = clienteRepository.findById(idCliente).orElse(null);

        if (cliente == null) {
            return "";
        }

        Usuario usuario = usuarioRepository.findById(cliente.getNoDocumento()).orElse(null);
        return usuario == null ? "" : usuario.getCorreo();
    }

    public String nombreVendedor(Long idVendedor) {
        Vendedor vendedor = vendedorRepository.findById(idVendedor).orElse(null);

        if (vendedor == null) {
            return "Sin vendedor";
        }

        Usuario usuario = usuarioRepository.findById(vendedor.getNoDocumento()).orElse(null);
        return usuario == null ? "Sin vendedor" : usuario.getNombre();
    }
}
