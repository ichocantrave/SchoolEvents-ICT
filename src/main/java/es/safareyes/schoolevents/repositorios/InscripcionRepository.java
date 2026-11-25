package es.safareyes.schoolevents.repositorios;

import es.safareyes.schoolevents.modelos.EstadoInscripcion;
import es.safareyes.schoolevents.modelos.Inscripcion;
import es.safareyes.schoolevents.modelos.TipoInscripcion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;import es.safareyes.schoolevents.dto.ParticipacionView;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;


public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {
    // Q1: consults derivadas
    //inscrip user in event (solo 1)
    Optional<Inscripcion> findByUsuarioIdAndEventoId(Integer usuarioId, Integer eventoId);
    //how inscrip tiene 1 event(ejemplito: EN_ESPERA)
    long countByEventoIdAndEstado(Integer eventoId, EstadoInscripcion estado);
    // First of the wait list
    Optional<Inscripcion> findFirstByEventoIdAndEstadoOrderByCreadaEnAsc(Integer eventoId, EstadoInscripcion estado);
    // Mi agenda: mis inscrip one tipo (endpoint 11)
    List<Inscripcion> findByUsuarioIdAndTipo(Integer usuarioId, TipoInscripcion tipo);
    //  Q6: paginación (endpoint 12)
    Page<Inscripcion> findByEventoId(Integer eventoId, Pageable pageable);
    //GET 3:  pa contar person que tienen su plaza in event
    long countByEventoIdAndTipoAndEstado(
            Integer eventoId,
            TipoInscripcion tipo,
            EstadoInscripcion estado
    );
    // ENDPOINT 5: obtain inscri to send noti
    List<Inscripcion> findByEventoId(Integer eventoId);

    // ENDPOINT 16: informe parti x cate & curso
    @Query(value = """
            SELECT
                c.nombre AS "categoria",
                cu.nombre AS "curso",
                COUNT(i.id_inscripcion) AS "totalParticipantes"
            FROM inscripcion i
            JOIN evento e ON i.id_evento = e.id_evento
            JOIN categoria c ON e.id_categoria = c.id_categoria
            JOIN usuario u ON i.id_usuario = u.id_usuario
            JOIN curso cu ON u.id_curso = cu.id_curso
            WHERE e.estado = 'FINALIZADO'
              AND i.tipo = 'ASISTIRE'
              AND i.estado = 'CONFIRMADA'
              AND i.asistio = true
              AND e.fecha_inicio >= :desde
              AND e.fecha_inicio <= :hasta
            GROUP BY c.nombre, cu.nombre
            ORDER BY c.nombre, cu.nombre
            """, nativeQuery = true)
    List<ParticipacionView> informeParticipacion(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );

}
