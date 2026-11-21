
package es.safareyes.schoolevents.dto;

import es.safareyes.schoolevents.modelos.EstadoEvento;
import java.time.LocalDateTime;
import java.util.List;

public record EventoDetalleDTO(
        Integer id,
        String titulo,
        String descripcion,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        LocalDateTime fechaLimite,
        EstadoEvento estado,
        Integer aforo,
        long plazasLibres,
        String lugar,
        String categoria,
        List<Integer> cursos,
        List<Integer> requisitos
) {
}
