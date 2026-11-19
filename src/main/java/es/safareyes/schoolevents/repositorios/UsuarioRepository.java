package es.safareyes.schoolevents.repositorios;

import es.safareyes.schoolevents.modelos.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByUsuario(String usuario);
    //busca un único nombre de usuario en la base de datos a partir de su nombre de usuario.
}
