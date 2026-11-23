package es.safareyes.schoolevents.repositorios;

import es.safareyes.schoolevents.modelos.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionRepository
        extends JpaRepository<Notificacion, Integer> {
}
