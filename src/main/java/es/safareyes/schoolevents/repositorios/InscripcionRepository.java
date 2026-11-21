package es.safareyes.schoolevents.repositorios;

import es.safareyes.schoolevents.modelos.EstadoInscripcion;
import es.safareyes.schoolevents.modelos.Inscripcion;
import es.safareyes.schoolevents.modelos.TipoInscripcion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import es.safareyes.schoolevents.modelos.TipoInscripcion;
import es.safareyes.schoolevents.modelos.EstadoInscripcion;
import java.util.List;
import java.util.Optional;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {

    // Q1: consultas derivadas

    // La inscripción de un usuario en un evento (solo puede haber una)
    Optional<Inscripcion> findByUsuarioIdAndEventoId(Integer usuarioId, Integer eventoId);

    // Cuántas inscripciones tiene un evento en un estado (ejemplito: EN_ESPERA)
    long countByEventoIdAndEstado(Integer eventoId, EstadoInscripcion estado);

    // El primero de la lista de espera
    Optional<Inscripcion> findFirstByEventoIdAndEstadoOrderByCreadaEnAsc(Integer eventoId, EstadoInscripcion estado);

    // Mi agenda: mis inscripciones de un tipo (endpoint 11)
    List<Inscripcion> findByUsuarioIdAndTipo(Integer usuarioId, TipoInscripcion tipo);

    //  Q6: paginación (endpoint 12)
    Page<Inscripcion> findByEventoId(Integer eventoId, Pageable pageable);

    //GET 3:  pa contar person que tienen su plaza in event
    long countByEventoIdAndTipoAndEstado(
            Integer eventoId,
            TipoInscripcion tipo,
            EstadoInscripcion estado
    );

}
