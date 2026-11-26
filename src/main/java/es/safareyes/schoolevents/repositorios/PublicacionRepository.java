package es.safareyes.schoolevents.repositorios;

import es.safareyes.schoolevents.modelos.Publicacion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicacionRepository extends JpaRepository<Publicacion, Integer> {
    // Q8 (endpoint 15): publicaciones of event w fotos in one consult
    @EntityGraph(attributePaths = {"fotos"})
    List<Publicacion> findByEventoId(Integer eventoId);

    //find all las publicaciones asociadas en un event especific, using @EntityGraph to cargar fotos relacionas de forma eficiente en una sola
    //consult to BD.
}
