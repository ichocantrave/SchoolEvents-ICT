package es.safareyes.schoolevents.controladores;

import es.safareyes.schoolevents.modelos.Publicacion;
import es.safareyes.schoolevents.servicios.PublicacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/eventos")
public class PublicacionController {
    private final PublicacionService servicio;
    public PublicacionController(PublicacionService servicio){
        this.servicio = servicio;
    }
    //Endpoint 14: public results
    @PostMapping("/{id}/publicaciones")
    public ResponseEntity<Map<String, Object>> crear(
            @PathVariable Integer id,
            @RequestParam Integer idUsuario,
            @RequestBody Map<String, String> datos){
        Publicacion publicacion = servicio.crear(id, idUsuario, datos.get("texto"));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", publicacion.getId(),
                "ideEvento", publicacion.getEvento().getId(),
                "texto", publicacion.getTexto()
                )
        );
    }
    // ENDPOINT 15: consult publi event
    @GetMapping("/{id}/publicaciones")
    public List<Map<String, Object>> listarPublicaciones(
            @PathVariable Integer id) {

        return servicio.listarPublicaciones(id);
    }
}