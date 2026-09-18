package com.main.NexShop.Service;

import com.main.NexShop.Model.Producto;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CarritoService {

    private final String CLAVE_CARRITO = "carrito";

    @SuppressWarnings("unchecked")
    public Map<Long, Integer> obtener(HttpSession session) {
        Map<Long, Integer> carrito = (Map<Long, Integer>) session.getAttribute(CLAVE_CARRITO);

        if (carrito == null) {
            carrito = new LinkedHashMap<>();
            session.setAttribute(CLAVE_CARRITO, carrito);
        }

        return carrito;
    }

    public void agregar(HttpSession session, Long idProducto) {
        Map<Long, Integer> carrito = obtener(session);
        int cantidad = carrito.getOrDefault(idProducto, 0);
        carrito.put(idProducto, cantidad + 1);
    }

    public void restar(HttpSession session, Long idProducto) {
        Map<Long, Integer> carrito = obtener(session);

        if (!carrito.containsKey(idProducto)) {
            return;
        }

        int cantidad = carrito.get(idProducto) - 1;

        if (cantidad <= 0) {
            carrito.remove(idProducto);
        } else {
            carrito.put(idProducto, cantidad);
        }
    }

    public void eliminar(HttpSession session, Long idProducto) {
        obtener(session).remove(idProducto);
    }

    public void vaciar(HttpSession session) {
        obtener(session).clear();
    }

    public double subtotal(HttpSession session, ProductoService productoService) {
        double total = 0;
        Map<Long, Integer> carrito = obtener(session);

        for (Long id : carrito.keySet()) {
            Producto producto = productoService.obtenerPorId(id);

            if (producto != null) {
                total += producto.getPrecio() * carrito.get(id);
            }
        }

        return total;
    }
}
