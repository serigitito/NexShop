package com.main.NexShop.Service;

import com.main.NexShop.Model.Producto;
import com.main.NexShop.Repository.DetalleVentaRepository;
import com.main.NexShop.Repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    public List<Producto> obtenerTodos() {
        return productoRepository.findAllByOrderByIdProductoDesc();
    }

    public List<Producto> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            return obtenerTodos();
        }
        return productoRepository.findByNombreContainingIgnoreCaseOrderByIdProductoDesc(texto);
    }

    public List<String> categorias() {
        List<String> categorias = new ArrayList<>();

        for (Producto producto : productoRepository.findAll()) {
            if (producto.getCategoria() != null && !categorias.contains(producto.getCategoria())) {
                categorias.add(producto.getCategoria());
            }
        }

        categorias.sort(String::compareToIgnoreCase);
        return categorias;
    }

    public List<Producto> porCategoria(String categoria) {
        return productoRepository.findByCategoriaOrderByIdProductoDesc(categoria);
    }

    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    public Producto obtenerPorIdYVendedor(Long id, Long idVendedor) {
        Producto producto = obtenerPorId(id);

        if (producto != null && idVendedor.equals(producto.getIdVendedor())) {
            return producto;
        }

        return null;
    }

    public List<Producto> obtenerPorVendedor(Long idVendedor) {
        return productoRepository.findByIdVendedorOrderByIdProductoDesc(idVendedor);
    }

    public Producto crear(Producto producto) {
        return productoRepository.save(producto);
    }

    public boolean actualizar(Long id, Long idVendedor, String nombre, Double precio,
                              String categoria, String imagen) {
        Producto producto = obtenerPorIdYVendedor(id, idVendedor);

        if (producto == null) {
            return false;
        }

        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setCategoria(categoria);

        if (imagen != null && !imagen.isBlank()) {
            producto.setImagen(imagen);
        }

        productoRepository.save(producto);
        return true;
    }

    public String eliminar(Long id, Long idVendedor) {
        Producto producto = obtenerPorIdYVendedor(id, idVendedor);

        if (producto == null) {
            return "no_encontrado";
        }

        if (detalleVentaRepository.countByIdProducto(id) > 0) {
            return "tiene_ventas";
        }

        productoRepository.delete(producto);
        return "ok";
    }

    public List<Object[]> categoriasConConteo() {
        List<Object[]> resultado = new ArrayList<>();

        for (String categoria : categorias()) {
            int cantidad = productoRepository.findByCategoria(categoria).size();
            resultado.add(new Object[]{categoria, cantidad});
        }

        return resultado;
    }

    public void renombrarCategoria(String actual, String nueva) {
        List<Producto> productos = productoRepository.findByCategoria(actual);

        for (Producto producto : productos) {
            producto.setCategoria(nueva);
            productoRepository.save(producto);
        }
    }
}
