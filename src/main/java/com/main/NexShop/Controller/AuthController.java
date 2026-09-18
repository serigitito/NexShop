package com.main.NexShop.Controller;

import com.main.NexShop.Model.Usuario;
import com.main.NexShop.Service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "autenticacion/login";
    }

    @GetMapping("/registro")
    public String registro() {
        return "autenticacion/registro";
    }

    @PostMapping("/auth/login")
    public String procesarLogin(
            @RequestParam("usuario") String usuario,
            @RequestParam("contrasena") String contrasena,
            HttpSession session) {

        Usuario user = usuarioService.buscarPorUsuario(usuario);

        if (user == null || !passwordEncoder.matches(contrasena, user.getContrasena())) {
            return "redirect:/login?error=credenciales";
        }

        if ("bloqueado".equalsIgnoreCase(user.getEstado())) {
            return "redirect:/login?error=bloqueado";
        }

        session.setAttribute("id", user.getNoDocumento());
        session.setAttribute("nombre", user.getNombre());
        session.setAttribute("usuario", user.getUsuario());

        String rol = user.getRol();

        if (rol == null || rol.isBlank()) {
            rol = "usuario";
        }

        rol = rol.toLowerCase();
        session.setAttribute("rol", rol);

        if ("admin".equals(rol)) {
            return "redirect:/admin/dashboard";
        }

        return "redirect:/tienda";
    }

    @PostMapping("/auth/registro")
    public String procesarRegistro(
            @RequestParam("documento") Long documento,
            @RequestParam("nombre") String nombre,
            @RequestParam("usuario") String usuario,
            @RequestParam("correo") String correo,
            @RequestParam("telefono") String telefono,
            @RequestParam("direccion") String direccion,
            @RequestParam("contrasena") String contrasena) {

        if (usuarioService.existeUsuario(usuario, documento, correo)) {
            return "redirect:/registro?error=existe";
        }

        if (contrasena.trim().length() < 8) {
            return "redirect:/registro?error=password";
        }

        Usuario nuevo = new Usuario();

        nuevo.setNoDocumento(documento);
        nuevo.setNombre(nombre.trim());
        nuevo.setUsuario(usuario.trim());
        nuevo.setCorreo(correo.trim());
        nuevo.setTelefono(telefono.trim());
        nuevo.setDireccion(direccion.trim());
        nuevo.setContrasena(contrasena.trim());
        nuevo.setRol("usuario");
        nuevo.setEstado("activo");

        usuarioService.registrar(nuevo);

        usuarioService.crearCliente(documento);
        usuarioService.crearVendedor(documento);

        return "redirect:/login?registro=ok";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}