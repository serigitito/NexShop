package com.main.NexShop.Controller;

import com.main.NexShop.Model.Producto;
import com.main.NexShop.Service.CarritoService;
import com.main.NexShop.Service.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class CarritoController {

    @Autowired private CarritoService carritoService;
    @Autowired private ProductoService productoService;

    @GetMapping("/carrito")
    public String carrito(HttpSession session, Model model) {
        Map<Long, Integer> carrito = carritoService.obtener(session);
        List<ProductoCarrito> productos = new ArrayList<>();
        double subtotal = 0;

        for (Long id : carrito.keySet()) {
            Producto producto = productoService.obtenerPorId(id);

            if (producto != null) {
                int cantidad = carrito.get(id);
                double valor = producto.getPrecio() * cantidad;
                subtotal += valor;
                productos.add(new ProductoCarrito(producto, cantidad, valor));
            }
        }

        model.addAttribute("productosCarrito", productos);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("iva", subtotal * 0.19);
        model.addAttribute("total", subtotal * 1.19);
        model.addAttribute("cartCount", carrito.size());

        return "tienda/carrito";
    }

    @GetMapping("/carrito/agregar")
    public String agregar(@RequestParam("id") Long id, HttpSession session) {
        carritoService.agregar(session, id);
        return "redirect:/carrito";
    }

    @GetMapping("/carrito/sumar")
    public String sumar(@RequestParam("id") Long id, HttpSession session) {
        carritoService.agregar(session, id);
        return "redirect:/carrito";
    }

    @GetMapping("/carrito/restar")
    public String restar(@RequestParam("id") Long id, HttpSession session) {
        carritoService.restar(session, id);
        return "redirect:/carrito";
    }

    @GetMapping("/carrito/eliminar")
    public String eliminar(@RequestParam("id") Long id, HttpSession session) {
        carritoService.eliminar(session, id);
        return "redirect:/carrito";
    }

    @GetMapping("/carrito/vaciar")
    public String vaciar(HttpSession session) {
        carritoService.vaciar(session);
        return "redirect:/carrito";
    }

    public static class ProductoCarrito {
        private Producto producto;
        private int cantidad;
        private double subtotal;

        public ProductoCarrito(Producto producto, int cantidad, double subtotal) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.subtotal = subtotal;
        }

        public Producto getProducto() { return producto; }
        public int getCantidad() { return cantidad; }
        public double getSubtotal() { return subtotal; }
    }
}
