package es.safareyes.schoolevents.repositorios;

import es.safareyes.schoolevents.modelos.Requisito;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequisitoRepository
        extends JpaRepository<Requisito, Integer> {
}
