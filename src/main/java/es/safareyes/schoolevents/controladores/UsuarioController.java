package es.safareyes.schoolevents.controladores;

import es.safareyes.schoolevents.modelos.TipoInscripcion;
import es.safareyes.schoolevents.servicios.InscripcionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {
    private final InscripcionService servicio;
    public UsuarioController(InscripcionService servicio){
        this.servicio = servicio;
    }

    //Enpoint 11: consult mis events
    @GetMapping("/me/eventos")
    public List<Map<String, Object>> miAgenda(
            @RequestParam Integer idUsuario,
            @RequestParam TipoInscripcion tipo){
        return servicio.miAgenda(idUsuario, tipo);
    }
}
