package com.main.NexShop.Controller;

import com.main.NexShop.Service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PerfilController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/perfil")
    public String perfil(HttpSession session, Model model) {
        if (session.getAttribute("id") == null) {
            return "redirect:/login";
        }

        Long documento = (Long) session.getAttribute("id");
        model.addAttribute("usuarioPerfil", usuarioService.getUsuarioById(documento));
        return "autenticacion/perfil";
    }

    @GetMapping("/perfil/editar")
    public String editar(HttpSession session, Model model) {
        if (session.getAttribute("id") == null) {
            return "redirect:/login";
        }

        Long documento = (Long) session.getAttribute("id");
        model.addAttribute("usuarioPerfil", usuarioService.getUsuarioById(documento));
        return "autenticacion/perfil_editar";
    }

    @PostMapping("/perfil/actualizar")
    public String actualizar(
            @RequestParam(name = "nombre") String nombre,
            @RequestParam(name = "usuario") String usuario,
            @RequestParam(name = "correo") String correo,
            @RequestParam(name = "telefono") String telefono,
            @RequestParam(name = "direccion") String direccion,
            @RequestParam(name = "contrasena", required = false, defaultValue = "") String contrasena,
            HttpSession session) {

        if (session.getAttribute("id") == null) {
            return "redirect:/login";
        }

        if (nombre.isBlank() || usuario.isBlank() || correo.isBlank()
                || telefono.isBlank() || direccion.isBlank()) {
            return "redirect:/perfil/editar?error=datos";
        }

        Long documento = (Long) session.getAttribute("id");

        if (usuarioService.existeOtroUsuario(usuario, correo, documento)) {
            return "redirect:/perfil/editar?error=duplicado";
        }

        if (!contrasena.isBlank() && contrasena.trim().length() < 8) {
            return "redirect:/perfil/editar?error=password";
        }

        usuarioService.actualizar(
                documento,
                nombre.trim(),
                usuario.trim(),
                correo.trim(),
                telefono.trim(),
                direccion.trim(),
                contrasena
        );

        session.setAttribute("nombre", nombre.trim());
        session.setAttribute("usuario", usuario.trim());

        return "redirect:/perfil?actualizado=1";
    }
}
