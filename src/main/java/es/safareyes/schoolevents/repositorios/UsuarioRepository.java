package es.safareyes.schoolevents.repositorios;

import es.safareyes.schoolevents.modelos.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByUsuario(String usuario);
    //find 1 only name of user in BD a partir de su name o user.
}
