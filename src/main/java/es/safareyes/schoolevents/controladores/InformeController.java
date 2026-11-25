package es.safareyes.schoolevents.controladores;

import es.safareyes.schoolevents.dto.ParticipacionView;
import es.safareyes.schoolevents.servicios.InformeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/informes")
public class InformeController {
    private final InformeService servicio;
    public InformeController(InformeService servicio) {
        this.servicio = servicio;
    }
    // ENDPOINT 16: infor partic
    @GetMapping("/participacion")
    public List<ParticipacionView> participacion(
            @RequestParam Integer idUsuario,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime desde,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime hasta) {
        return servicio.participacion(idUsuario, desde, hasta);
    }
}

