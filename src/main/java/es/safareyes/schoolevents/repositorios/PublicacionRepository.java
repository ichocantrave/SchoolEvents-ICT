package es.safareyes.schoolevents.repositorios;

import es.safareyes.schoolevents.modelos.Publicacion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicacionRepository extends JpaRepository<Publicacion, Integer> {

    // Q8 (endpoint 15): publicaciones de un evento con sus fotos en una sola consulta
    @EntityGraph(attributePaths = {"fotos"})
    List<Publicacion> findByEventoId(Integer eventoId);

    //busca todas las publicaaciones asociadas en un evento especifico, utilizando un @EntityGraph para cargar las fotos relacionadas de forma eficiente en una sola
    //consulta a la base de datos.
}
