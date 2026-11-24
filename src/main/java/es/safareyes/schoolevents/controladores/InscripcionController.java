package es.safareyes.schoolevents.controladores;

import es.safareyes.schoolevents.modelos.Inscripcion;
import es.safareyes.schoolevents.modelos.TipoInscripcion;
import es.safareyes.schoolevents.servicios.InscripcionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/eventos")
public class InscripcionController {
    private final InscripcionService servicio;
    public InscripcionController(InscripcionService servicio){
        this.servicio = servicio;
    }
    //Endpoint 9: inscrip alumn a event
    //er idUser es pa las pruebaas w Postman
    @PostMapping("/{id}/inscripciones")
    public ResponseEntity<Map<String, Object>> inscribir(
            @PathVariable Integer id,
            @RequestParam Integer idUsuario,
            @RequestBody Map<String, TipoInscripcion> datos){
     Inscripcion inscripcion = servicio.inscribir(id, idUsuario,datos.get("tipo")
     );
     return ResponseEntity.ok(
             Map.of(
                     "id", inscripcion.getId(),
                     "tipo",inscripcion.getTipo().name(),
                     "estado", inscripcion.getEstado().name())
     );
    }
    // ENDPOINT 10: delete inscrip
    // idUsuario solo pa test Postman
    @DeleteMapping("/{id}/inscripciones/me")
    public ResponseEntity<Void> borrarInscripcion(
            @PathVariable Integer id,
            @RequestParam Integer idUsuario) {

        servicio.borrarInscripcion(id, idUsuario);

        return ResponseEntity.noContent().build();
    }
}
