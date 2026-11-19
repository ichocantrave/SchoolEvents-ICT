package es.safareyes.schoolevents.repositorios;

import es.safareyes.schoolevents.modelos.Lugar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LugarRepository extends JpaRepository<Lugar, Integer> {
}
