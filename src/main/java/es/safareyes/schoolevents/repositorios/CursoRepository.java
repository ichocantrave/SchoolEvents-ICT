package es.safareyes.schoolevents.repositorios;

import es.safareyes.schoolevents.modelos.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, Integer> {
}
