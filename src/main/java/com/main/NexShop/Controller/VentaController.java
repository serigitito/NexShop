package com.main.NexShop.Controller;

import com.main.NexShop.Model.Producto;
import com.main.NexShop.Model.Vendedor;
import com.main.NexShop.Model.Venta;
import com.main.NexShop.Service.ProductoService;
import com.main.NexShop.Service.UsuarioService;
import com.main.NexShop.Service.VentaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Controller
@RequestMapping("/vendedor")
public class VentaController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private VentaService ventaService;

    private boolean esVendedor(HttpSession session) {
        return "vendedor".equals(session.getAttribute("rol"));
    }

    private Long idVendedor(HttpSession session) {
        Long documento = (Long) session.getAttribute("id");
        Vendedor vendedor = usuarioService.obtenerVendedor(documento);

        if (vendedor == null) {
            return null;
        }

        return vendedor.getIdVendedor();
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        if (!esVendedor(session)) {
            return "redirect:/tienda";
        }

        Long id = idVendedor(session);

        if (id == null) {
            return "redirect:/logout";
        }

        List<Producto> productos = productoService.obtenerPorVendedor(id);
        List<Venta> ventas = ventaService.listarVentasVendedor(id, "");

        int totalVentas = 0;
        double totalIngresos = 0;

        for (Venta venta : ventas) {
            if ("Pagada".equals(venta.getEstado())) {
                totalVentas++;
                totalIngresos += venta.getCostoFinal();
            }
        }

        model.addAttribute("totalProductos", productos.size());
        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("totalIngresos", totalIngresos);
        model.addAttribute("ventas", ventas);

        return "vendedor/dashboard";
    }

    @GetMapping("/productos")
    public String productos(HttpSession session, Model model) {

        if (!esVendedor(session)) {
            return "redirect:/tienda";
        }

        model.addAttribute(
                "productos",
                productoService.obtenerPorVendedor(idVendedor(session))
        );

        return "vendedor/productos";
    }

    @GetMapping("/producto/nuevo")
    public String nuevo(HttpSession session, Model model) {

        if (!esVendedor(session)) {
            return "redirect:/tienda";
        }

        model.addAttribute("producto", null);

        return "vendedor/producto_form";
    }

    @GetMapping("/producto/editar")
    public String editar(
            @RequestParam("id") Long id,
            HttpSession session,
            Model model
    ) {

        if (!esVendedor(session)) {
            return "redirect:/tienda";
        }

        Producto producto = productoService.obtenerPorIdYVendedor(
                id,
                idVendedor(session)
        );

        if (producto == null) {
            return "redirect:/vendedor/productos";
        }

        model.addAttribute("producto", producto);

        return "vendedor/producto_form";
    }

    @PostMapping("/producto/guardar")
    public String guardar(
            @RequestParam(defaultValue = "0") Long idProducto,
            @RequestParam("nombre") String nombre,
            @RequestParam("precio") Double precio,
            @RequestParam("categoria") String categoria,
            @RequestParam(required = false) MultipartFile imagen,
            HttpSession session
    ) throws IOException {

        if (!esVendedor(session)) {
            return "redirect:/tienda";
        }

        if (nombre.isBlank() || precio <= 0 || categoria.isBlank()) {
            return "redirect:/vendedor/productos?error=datos";
        }

        Long idVendedor = idVendedor(session);

        String ruta = guardarImagen(imagen);

        if (idProducto > 0) {

            productoService.actualizar(
                    idProducto,
                    idVendedor,
                    nombre.trim(),
                    precio,
                    categoria.trim(),
                    ruta
            );

        } else {

            if (ruta == null) {
                return "redirect:/vendedor/producto/nuevo?error=imagen";
            }

            Producto producto = new Producto();

            producto.setNombre(nombre.trim());
            producto.setPrecio(precio);
            producto.setCategoria(categoria.trim());
            producto.setImagen(ruta);
            producto.setIdVendedor(idVendedor);

            productoService.crear(producto);
        }

        return "redirect:/vendedor/productos";
    }

    @GetMapping("/producto/eliminar")
    public String eliminar(
            @RequestParam("id") Long id,
            HttpSession session
    ) {

        if (!esVendedor(session)) {
            return "redirect:/tienda";
        }

        String resultado = productoService.eliminar(
                id,
                idVendedor(session)
        );

        if ("tiene_ventas".equals(resultado)) {
            return "redirect:/vendedor/productos?error=tiene_ventas";
        }

        return "redirect:/vendedor/productos";
    }

    @GetMapping("/pedidos")
    public String pedidos(
            @RequestParam(name = "q", defaultValue = "") String q,
            HttpSession session,
            Model model
    ) {

        if (!esVendedor(session)) {
            return "redirect:/tienda";
        }

        model.addAttribute("busqueda", q);

        model.addAttribute(
                "pedidos",
                ventaService.listarVentasVendedor(
                        idVendedor(session),
                        q
                )
        );

        return "vendedor/pedidos";
    }

    @GetMapping("/pedido/estado")
    public String pedidoEstado(
            @RequestParam("id") Long id,
            @RequestParam("estado") String estado,
            HttpSession session
    ) {

        if (!esVendedor(session)) {
            return "redirect:/tienda";
        }

        if (!(
                estado.equals("Preparando")
                        || estado.equals("Enviado")
                        || estado.equals("Entregado")
        )) {
            return "redirect:/vendedor/pedidos";
        }

        Venta venta = ventaService.obtenerVenta(id);

        Long idVendedor = idVendedor(session);

        if (
                venta != null
                        && idVendedor != null
                        && idVendedor.equals(venta.getIdVendedor())
        ) {

            venta.setEstadoEnvio(estado);

            ventaService.actualizarVentaEnvio(venta);
        }

        return "redirect:/vendedor/pedidos";
    }

    @GetMapping("/ventas")
    public String ventas(
            HttpSession session,
            Model model
    ) {

        if (!esVendedor(session)) {
            return "redirect:/tienda";
        }

        model.addAttribute(
                "ventas",
                ventaService.listarVentasVendedor(
                        idVendedor(session),
                        ""
                )
        );

        return "vendedor/ventas";
    }

    @GetMapping("/reporte/ventas")
    public ResponseEntity<byte[]> reporteVentas(
            HttpSession session
    ) {

        if (!esVendedor(session)) {
            return ResponseEntity.status(403).build();
        }

        List<Venta> ventas = ventaService.listarVentasVendedor(
                idVendedor(session),
                ""
        );

        StringBuilder csv = new StringBuilder("\uFEFF");

        csv.append("ID Venta;Fecha;Estado;Método Pago;Total\n");

        double total = 0;

        for (Venta venta : ventas) {

            csv.append(venta.getIdVenta()).append(';')
                    .append(venta.getFecha()).append(';')
                    .append(venta.getEstado()).append(';')
                    .append(venta.getMetodoPago()).append(';')
                    .append(venta.getCostoFinal()).append('\n');

            total += venta.getCostoFinal();
        }

        csv.append("\n;;;TOTAL;")
                .append(total);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=reporte_ventas.csv"
                )
                .contentType(
                        MediaType.parseMediaType("text/csv;charset=UTF-8")
                )
                .body(
                        csv.toString().getBytes(StandardCharsets.UTF_8)
                );
    }

    @GetMapping("/reporte/productos")
    public ResponseEntity<byte[]> reporteProductos(
            HttpSession session
    ) {

        if (!esVendedor(session)) {
            return ResponseEntity.status(403).build();
        }

        List<Producto> productos = productoService.obtenerPorVendedor(
                idVendedor(session)
        );

        StringBuilder csv = new StringBuilder("\uFEFF");

        csv.append("ID;Nombre;Categoría;Precio;Imagen\n");

        double inventario = 0;

        for (Producto p : productos) {

            csv.append(p.getIdProducto()).append(';')
                    .append(p.getNombre()).append(';')
                    .append(p.getCategoria()).append(';')
                    .append(p.getPrecio()).append(';')
                    .append(p.getImagen()).append('\n');

            inventario += p.getPrecio();
        }

        csv.append("\nTOTAL PRODUCTOS;")
                .append(productos.size())
                .append('\n');

        csv.append("VALOR INVENTARIO;")
                .append(inventario);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=reporte_productos.csv"
                )
                .contentType(
                        MediaType.parseMediaType("text/csv;charset=UTF-8")
                )
                .body(
                        csv.toString().getBytes(StandardCharsets.UTF_8)
                );
    }

    private String guardarImagen(
            MultipartFile archivo
    ) throws IOException {

        if (archivo == null || archivo.isEmpty()) {
            return null;
        }

        String nombre = archivo.getOriginalFilename();

        if (nombre == null) {
            return null;
        }

        int punto = nombre.lastIndexOf('.');

        if (punto < 0) {
            return null;
        }

        String extension = nombre
                .substring(punto + 1)
                .toLowerCase();

        if (!(
                extension.equals("jpg")
                        || extension.equals("jpeg")
                        || extension.equals("png")
                        || extension.equals("webp")
        )) {
            return null;
        }

        String archivoNuevo =
                "producto_"
                        + System.currentTimeMillis()
                        + "."
                        + extension;

        Path carpeta = Paths.get(
                "src/main/resources/static/img"
        );

        Files.createDirectories(carpeta);

        Files.copy(
                archivo.getInputStream(),
                carpeta.resolve(archivoNuevo),
                StandardCopyOption.REPLACE_EXISTING
        );

        return "/img/" + archivoNuevo;
    }
}
