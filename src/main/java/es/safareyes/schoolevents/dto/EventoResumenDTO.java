package es.safareyes.schoolevents.dto;

import es.safareyes.schoolevents.modelos.EstadoEvento;
import java.time.LocalDateTime;

public record EventoResumenDTO(
        Integer id,
        String titulo,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        EstadoEvento estado
) {
}
