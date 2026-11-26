package es.safareyes.schoolevents.repositorios;

import es.safareyes.schoolevents.modelos.EstadoEvento;
import es.safareyes.schoolevents.modelos.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import es.safareyes.schoolevents.dto.EventoCalendarioView;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento, Integer>, JpaSpecificationExecutor<Evento> {
    // Eventos de un estado, del más próximo al más lejano
    List<Evento> findByEstadoOrderByFechaInicioAsc(EstadoEvento estado);
    // Eventos de un estado entre dos fechas
    List<Evento> findByEstadoAndFechaInicioBetweenOrderByFechaInicioAsc(EstadoEvento estado,
                                                                        LocalDateTime desde,
                                                                        LocalDateTime hasta);
    // Búsqueda por texto en el título
    List<Evento> findByTituloContainingIgnoreCase(String texto);
    // Eventos de una categoría, por nombre
    @Query("SELECT e FROM Evento e WHERE e.categoria.nombre = :nombre ORDER BY e.fechaInicio")
    List<Evento> buscarPorCategoria(@Param("nombre") String nombre);
    //nativo la consulta
    @Query(value = """
            SELECT e.id_evento                           AS "id",
                   e.titulo                              AS "titulo",
                   TO_CHAR(e.fecha_inicio, 'YYYY-MM-DD') AS "dia",
                   TO_CHAR(e.fecha_inicio, 'HH24:MI')    AS "horaInicio",
                   TO_CHAR(e.fecha_fin, 'HH24:MI')       AS "horaFin",
                   c.icono                               AS "icono",
                   c.color                               AS "color"
            FROM evento e
            JOIN categoria c ON c.id_categoria = e.id_categoria
            WHERE e.estado IN ('PUBLICADO', 'FINALIZADO')
              AND EXTRACT(MONTH FROM e.fecha_inicio) = :mes
              AND EXTRACT(YEAR FROM e.fecha_inicio) = :anio
            ORDER BY e.fecha_inicio
            """, nativeQuery = true)
    List<EventoCalendarioView> calendarioDelMes(@Param("mes") int mes, @Param("anio") int anio);

    // ---------- Q6: paginación (endpoint 1) ----------
    Page<Evento> findByEstado(EstadoEvento estado, Pageable pageable);

    // ---------- Q8: EntityGraph (endpoint 3: detalle) ----------
    // Trae el evento con su lugar, categoría y cursos en una sola consulta
    @EntityGraph(attributePaths = {"lugar", "categoria", "cursos"})
    Optional<Evento> findConDetalleById(Integer id);
    // Q4: pasa a FINALIZADO los eventos PUBLICADOS que ya terminaron
    @Modifying
    @Query("UPDATE Evento e SET e.estado = es.safareyes.schoolevents.modelos.EstadoEvento.FINALIZADO "
            + " WHERE e.estado = es.safareyes.schoolevents.modelos.EstadoEvento.PUBLICADO AND e.fechaFin < :ahora")
    int finalizarTerminados(@Param("ahora") LocalDateTime ahora);
    // Q2 JPQL: comprobar si hay eventos solapados en el mismo lugar
    @Query("""
       SELECT COUNT(e) FROM Evento e
       WHERE e.lugar.id = :idLugar
       AND e.estado <> :cancelado
       AND e.fechaInicio < :fin
       AND e.fechaFin > :inicio
       """)
    long contarSolapamientos(
            @Param("idLugar") Integer idLugar,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("cancelado") EstadoEvento cancelado
    );
    // Q2 JPQL: comprobar solapamientos al editar, ignorando el evento actual
    @Query("""
       SELECT COUNT(e) FROM Evento e
       WHERE e.id <> :idEvento
       AND e.lugar.id = :idLugar
       AND e.estado <> :cancelado
       AND e.fechaInicio < :fin
       AND e.fechaFin > :inicio
       """)
    long contarSolapamientosAlEditar(
            @Param("idEvento") Integer idEvento,
            @Param("idLugar") Integer idLugar,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("cancelado") EstadoEvento cancelado
    );


}
