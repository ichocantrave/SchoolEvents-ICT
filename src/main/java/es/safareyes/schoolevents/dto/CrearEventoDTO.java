package es.safareyes.schoolevents.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CrearEventoDTO(
        String titulo,
        String descripcion,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        LocalDateTime fechaLimite,
        Integer aforo,
        Integer idLugar,
        Integer idCategoria,
        List<Integer> cursos,
        List<Integer> requisitos
) {
}

