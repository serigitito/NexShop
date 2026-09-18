package com.main.NexShop.Controller;

import com.main.NexShop.Model.Producto;
import com.main.NexShop.Service.CarritoService;
import com.main.NexShop.Service.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CarritoService carritoService;

    @GetMapping("/")
    public String inicio() {
        return "redirect:/tienda";
    }

    @GetMapping("/tienda")
    public String tienda(
            @RequestParam(name = "categoria", required = false) String categoria,
            HttpSession session,
            Model model) {

        List<Producto> productos;

        if (categoria == null || categoria.isBlank()) {
            productos = productoService.obtenerTodos();
        } else {
            productos = productoService.porCategoria(categoria);
        }

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", productoService.categorias());
        model.addAttribute("cartCount", carritoService.obtener(session).size());

        return "tienda/index";
    }

    @GetMapping("/api/productos/buscar")
    @ResponseBody
    public List<Producto> buscar(@RequestParam("q") String q) {
        return productoService.buscar(q);
    }
}