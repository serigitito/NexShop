package com.main.NexShop.Controller;

import com.main.NexShop.Model.Cliente;
import com.main.NexShop.Model.Producto;
import com.main.NexShop.Model.Usuario;
import com.main.NexShop.Model.Venta;
import com.main.NexShop.Service.CarritoService;
import com.main.NexShop.Service.ProductoService;
import com.main.NexShop.Service.UsuarioService;
import com.main.NexShop.Service.VentaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.security.MessageDigest;
import java.util.Formatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class CheckoutController {

    private final String PAYU_API_KEY = "4Vj8eK4rloUd272L48hsrarnUA";
    private final String PAYU_MERCHANT_ID = "508029";
    private final String PAYU_ACCOUNT_ID = "512321";
    private final String PAYU_URL = "https://sandbox.checkout.payulatam.com/ppp-web-gateway-payu/";

    @Autowired private VentaService ventaService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private ProductoService productoService;
    @Autowired private CarritoService carritoService;

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        if (session.getAttribute("id") == null) return "redirect:/login";

        Map<Long, Integer> carrito = carritoService.obtener(session);
        if (carrito.isEmpty()) return "redirect:/carrito";

        Long documento = (Long) session.getAttribute("id");
        Cliente cliente = ventaService.obtenerClientePorDocumento(documento);
        Usuario usuario = usuarioService.getUsuarioById(documento);

        if (cliente == null || usuario == null) return "redirect:/tienda";

        double subtotal = carritoService.subtotal(session, productoService);
        model.addAttribute("cliente", usuario);
        model.addAttribute("total", subtotal * 1.19);
        return "tienda/checkout";
    }

    @PostMapping("/checkout/procesar")
    public String procesar(HttpSession session, Model model) {
        if (session.getAttribute("id") == null) return "redirect:/login";

        Map<Long, Integer> carrito = carritoService.obtener(session);
        if (carrito.isEmpty()) return "redirect:/carrito";

        Long documento = (Long) session.getAttribute("id");
        Usuario usuario = usuarioService.getUsuarioById(documento);
        double total = carritoService.subtotal(session, productoService) * 1.19;
        String referencia = "NEXSHOP_" + System.currentTimeMillis() + "_" + documento;
        String firmaTexto = PAYU_API_KEY + "~" + PAYU_MERCHANT_ID + "~" + referencia + "~" + total + "~COP";

        session.setAttribute("payu_referencia", referencia);
        session.setAttribute("payu_total", total);

        model.addAttribute("referenceCode", referencia);
        model.addAttribute("signature", md5(firmaTexto));
        model.addAttribute("total", total);
        model.addAttribute("cliente", usuario);
        model.addAttribute("payuUrl", PAYU_URL);
        model.addAttribute("merchantId", PAYU_MERCHANT_ID);
        model.addAttribute("accountId", PAYU_ACCOUNT_ID);
        return "tienda/payu";
    }

    @GetMapping("/checkout/respuesta")
    public String respuesta(@RequestParam(name="lapTransactionState", required = false) String lapTransactionState,
                            @RequestParam(name="referenceCode", required = false) String referenceCode,
                            @RequestParam(name="TX_VALUE", required = false, defaultValue = "0") String TX_VALUE,
                            HttpSession session, Model model) {

        String estado = lapTransactionState == null ? "DESCONOCIDO" : lapTransactionState;
        String guardada = (String) session.getAttribute("payu_referencia");

        if (guardada != null && guardada.equals(referenceCode)
                && "APPROVED".equals(estado) && !carritoService.obtener(session).isEmpty()) {

            Long documento = (Long) session.getAttribute("id");
            Cliente cliente = ventaService.obtenerClientePorDocumento(documento);
            Usuario usuario = usuarioService.getUsuarioById(documento);

            if (cliente == null || usuario == null) return "redirect:/carrito";

            Map<Long, Integer> carrito = carritoService.obtener(session);
            List<Long> ventas = new ArrayList<>();

            for (Long idProducto : carrito.keySet()) {
                Producto producto = productoService.obtenerPorId(idProducto);
                if (producto == null) continue;

                Venta venta = ventaService.crearVenta("PayU", cliente.getIdCliente(),
                        producto.getIdVendedor(), usuario.getDireccion());

                int cantidad = carrito.get(idProducto);
                ventaService.agregarDetalle(venta.getIdVenta(), idProducto, cantidad, producto.getPrecio());
                ventaService.actualizarTotales(venta.getIdVenta());

                ventaService.registrarPago(
                        venta.getIdVenta(), producto.getPrecio() * cantidad * 0.19,
                        "PayU", "Aprobado", "Pago aprobado por PayU - Ref: " + referenceCode);

                ventas.add(venta.getIdVenta());
            }

            carritoService.vaciar(session);
            session.setAttribute("ventas", ventas);
            session.removeAttribute("payu_referencia");
            session.removeAttribute("payu_total");
            return "redirect:/checkout/confirmacion";
        }

        model.addAttribute("estado", estado);
        model.addAttribute("referenceCode", referenceCode == null ? "—" : referenceCode);
        model.addAttribute("totalRespuesta", TX_VALUE);
        return "tienda/respuesta";
    }

    @GetMapping("/checkout/confirmacion")
    public String confirmacion(HttpSession session, Model model) {
        model.addAttribute("ventas", session.getAttribute("ventas"));
        return "tienda/confirmacion";
    }

    private String md5(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(texto.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            Formatter formatter = new Formatter();

            for (byte b : bytes) {
                formatter.format("%02x", b);
            }

            String resultado = formatter.toString();
            formatter.close();
            return resultado;
        } catch (Exception e) {
            return "";
        }
    }
}
