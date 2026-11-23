package es.safareyes.schoolevents.repositorios;

import es.safareyes.schoolevents.modelos.UsuarioNotificacion;
import es.safareyes.schoolevents.modelos.UsuarioNotificacionID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioNotificacionRepository
        extends JpaRepository<UsuarioNotificacion, UsuarioNotificacionID> {
}
