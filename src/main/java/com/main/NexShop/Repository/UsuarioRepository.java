package com.main.NexShop.Repository;

import com.main.NexShop.Model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Usuario findByUsuario(String usuario);
    Usuario findByCorreo(String correo);
    List<Usuario> findByRolOrderByNombre(String rol);
    List<Usuario> findAllByOrderByRolAscNombreAsc();
}
