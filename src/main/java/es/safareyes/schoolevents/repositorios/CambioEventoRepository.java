package es.safareyes.schoolevents.repositorios;

import es.safareyes.schoolevents.modelos.CambioEvento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CambioEventoRepository
        extends JpaRepository<CambioEvento, Integer> {
}

