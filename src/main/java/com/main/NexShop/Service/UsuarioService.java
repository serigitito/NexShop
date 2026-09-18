package com.main.NexShop.Service;

import com.main.NexShop.Model.Cliente;
import com.main.NexShop.Model.Usuario;
import com.main.NexShop.Model.Vendedor;
import com.main.NexShop.Repository.ClienteRepository;
import com.main.NexShop.Repository.UsuarioRepository;
import com.main.NexShop.Repository.VendedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VendedorRepository vendedorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario getUsuarioById(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public Usuario buscarPorUsuario(String usuario) {
        return usuarioRepository.findByUsuario(usuario);
    }

    public List<Usuario> listar(String rol) {
        if (rol == null || rol.isBlank()) {
            return usuarioRepository.findAllByOrderByRolAscNombreAsc();
        }
        return usuarioRepository.findByRolOrderByNombre(rol);
    }

    public Usuario createUsuario(Usuario usuario) {
        return registrar(usuario);
    }

    public Usuario registrar(Usuario usuario) {
        if (usuario.getContrasena() == null || usuario.getContrasena().trim().length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener mínimo 8 caracteres");
        }

        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena().trim()));
        return usuarioRepository.save(usuario);
    }

    public boolean existeUsuario(String usuario, Long documento, String correo) {
        return usuarioRepository.findByUsuario(usuario) != null
                || usuarioRepository.findByCorreo(correo) != null
                || usuarioRepository.findById(documento).isPresent();
    }

    public void crearCliente(Long documento) {
        Cliente cliente = new Cliente();
        cliente.setNoDocumento(documento);
        clienteRepository.save(cliente);
    }

    public void crearVendedor(Long documento) {
        Vendedor vendedor = new Vendedor();
        vendedor.setNoDocumento(documento);
        vendedorRepository.save(vendedor);
    }

    public Cliente obtenerCliente(Long documento) {
        return clienteRepository.findByNoDocumento(documento);
    }

    public Vendedor obtenerVendedor(Long documento) {
        return vendedorRepository.findByNoDocumento(documento);
    }

    public void cambiarEstado(Long documento, String estado) {
        Usuario usuario = getUsuarioById(documento);
        if (usuario != null) {
            usuario.setEstado(estado);
            usuarioRepository.save(usuario);
        }
    }

    public boolean existeOtroUsuario(String usuario, String correo, Long documentoActual) {
        Usuario porUsuario = usuarioRepository.findByUsuario(usuario);
        if (porUsuario != null && !porUsuario.getNoDocumento().equals(documentoActual)) {
            return true;
        }

        Usuario porCorreo = usuarioRepository.findByCorreo(correo);
        return porCorreo != null && !porCorreo.getNoDocumento().equals(documentoActual);
    }

    public void actualizar(Long documento, String nombre, String usuario, String correo,
                           String telefono, String direccion, String contrasena) {
        Usuario existente = getUsuarioById(documento);

        if (existente == null) {
            return;
        }

        existente.setNombre(nombre);
        existente.setUsuario(usuario);
        existente.setCorreo(correo);
        existente.setTelefono(telefono);
        existente.setDireccion(direccion);

        if (contrasena != null && !contrasena.isBlank() && contrasena.trim().length() >= 8) {
            existente.setContrasena(passwordEncoder.encode(contrasena.trim()));
        }

        usuarioRepository.save(existente);
    }

    public void deleteUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}
