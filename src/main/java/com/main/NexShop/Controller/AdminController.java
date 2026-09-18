package com.main.NexShop.Controller;

import com.main.NexShop.Model.Venta;
import com.main.NexShop.Service.AdminService;
import com.main.NexShop.Service.ProductoService;
import com.main.NexShop.Service.UsuarioService;
import com.main.NexShop.Service.VentaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private VentaService ventaService;

    private boolean esAdmin(HttpSession session) {
        return "admin".equals(session.getAttribute("rol"));
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/tienda";
        }

        model.addAttribute("stats", adminService.estadisticas());
        model.addAttribute("ventasRecientes", adminService.ventasRecientes(8));

        return "admin/dashboard";
    }

    @GetMapping("/usuarios")
    public String usuarios(
            @RequestParam(name = "rol", required = false) String rol,
            HttpSession session,
            Model model) {

        if (!esAdmin(session)) {
            return "redirect:/tienda";
        }

        model.addAttribute("filtroRol", rol);
        model.addAttribute("usuarios", usuarioService.listar(rol));

        return "admin/usuarios";
    }

    @GetMapping("/usuario/estado")
    public String cambiarEstado(
            @RequestParam("doc") Long doc,
            @RequestParam("estado") String estado,
            HttpSession session) {

        if (!esAdmin(session)) {
            return "redirect:/tienda";
        }

        if ("activo".equals(estado) || "bloqueado".equals(estado)) {
            usuarioService.cambiarEstado(doc, estado);
        }

        return "redirect:/admin/usuarios";
    }

    @GetMapping("/categorias")
    public String categorias(HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/tienda";
        }

        model.addAttribute("categorias", productoService.categoriasConConteo());

        return "admin/categorias";
    }

    @PostMapping("/categoria/renombrar")
    public String renombrar(
            @RequestParam("actual") String actual,
            @RequestParam("nueva") String nueva,
            HttpSession session) {

        if (!esAdmin(session)) {
            return "redirect:/tienda";
        }

        if (!actual.isBlank() && !nueva.isBlank()) {
            productoService.renombrarCategoria(
                    actual.trim(),
                    nueva.trim()
            );
        }

        return "redirect:/admin/categorias";
    }

    @GetMapping("/ventas")
    public String ventas(
            @RequestParam(name = "q", required = false, defaultValue = "") String q,
            HttpSession session,
            Model model) {

        if (!esAdmin(session)) {
            return "redirect:/tienda";
        }

        model.addAttribute("busqueda", q);
        model.addAttribute("ventas", adminService.listarVentas(q));

        return "admin/ventas";
    }

    @GetMapping("/venta/ver")
    public String verVenta(
            @RequestParam("id") Long id,
            HttpSession session,
            Model model) {

        if (!esAdmin(session)) {
            return "redirect:/tienda";
        }

        Venta venta = ventaService.obtenerVenta(id);

        if (venta == null) {
            return "redirect:/admin/ventas";
        }

        model.addAttribute("venta", venta);
        model.addAttribute("detalles", ventaService.obtenerDetalles(id));

        return "admin/ver_venta";
    }

    @GetMapping("/venta/editar")
    public String editarVenta(
            @RequestParam("id") Long id,
            HttpSession session,
            Model model) {

        if (!esAdmin(session)) {
            return "redirect:/tienda";
        }

        Venta venta = ventaService.obtenerVenta(id);

        if (venta == null) {
            return "redirect:/admin/ventas";
        }

        model.addAttribute("venta", venta);

        return "admin/editar_venta";
    }

    @PostMapping("/venta/editar")
    public String guardarVenta(
            @RequestParam("id") Long id,
            @RequestParam("estado") String estado,
            @RequestParam("metodoPago") String metodoPago,
            @RequestParam("direccionEnvio") String direccionEnvio,
            HttpSession session) {

        if (!esAdmin(session)) {
            return "redirect:/tienda";
        }

        ventaService.actualizarVenta(
                id,
                estado,
                metodoPago,
                direccionEnvio
        );

        return "redirect:/admin/ventas";
    }

    @GetMapping("/venta/eliminar")
    public String eliminarVenta(
            @RequestParam("id") Long id,
            HttpSession session) {

        if (!esAdmin(session)) {
            return "redirect:/tienda";
        }

        ventaService.eliminarVenta(id);

        return "redirect:/admin/ventas";
    }

    @GetMapping("/pagos")
    public String pagos(
            HttpSession session,
            Model model) {

        if (!esAdmin(session)) {
            return "redirect:/tienda";
        }

        model.addAttribute("pagos", ventaService.listarPagos());

        return "admin/pagos";
    }

    @GetMapping("/reportes")
    public String reportes(HttpSession session) {
        if (!esAdmin(session)) {
            return "redirect:/tienda";
        }

        return "admin/reportes";
    }
}
