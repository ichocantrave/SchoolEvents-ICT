package es.safareyes.schoolevents.controladores;

import es.safareyes.schoolevents.dto.EventoResumenDTO;
import es.safareyes.schoolevents.dto.EventoCalendarioView;
import es.safareyes.schoolevents.dto.EventoDetalleDTO;
import es.safareyes.schoolevents.dto.CrearEventoDTO;
import es.safareyes.schoolevents.modelos.EstadoEvento;
import es.safareyes.schoolevents.modelos.Evento;
import es.safareyes.schoolevents.modelos.Usuario;
import es.safareyes.schoolevents.repositorios.EventoRepository;
import es.safareyes.schoolevents.repositorios.EventoSpecifications;
import es.safareyes.schoolevents.repositorios.UsuarioRepository;
import es.safareyes.schoolevents.servicios.EventoService;
import es.safareyes.schoolevents.dto.EditarEventoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/eventos")
public class EventoController {
    private final EventoRepository repositorio;
    private final EventoService servicio;
    private final UsuarioRepository usuarioRepository;
    public EventoController(
            EventoRepository repositorio,
            EventoService servicio,
            UsuarioRepository usuarioRepository) {
        this.repositorio = repositorio;
        this.servicio = servicio;
        this.usuarioRepository = usuarioRepository;
    }
    @GetMapping
    public Page<EventoResumenDTO> listar(
            // La famosa paginación
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "5") int tamano,
            // Filtritos opcionals
            @RequestParam(required = false) Integer categoria,
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Integer curso,
            // Fechitas opcinals
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime hasta) {
        //filtro to consults events public
        Specification<Evento> filtros =
                (root, query, cb) -> // he añadido listar para que gestione los dos filtros creados en eventoSpecification.java
                        cb.equal(root.get("estado"), EstadoEvento.PUBLICADO);
        // Filtramos x catgeoria  si recibimos  ID
        if (categoria != null) {
            filtros = filtros.and(EventoSpecifications.conCategoria(categoria)
            );
        }
        // Filtramos x titulo  si recibimos text
        if (texto != null && !texto.isBlank()) {
            filtros = filtros.and(EventoSpecifications.tituloContiene(texto)
            );
        }
        // Filtramos x curso si recibimos ID
        if (curso != null) {
            filtros = filtros.and(EventoSpecifications.conCurso(curso)
            );
        }
        // Filtramos x fecha si recibimos ambas
        if (desde != null && hasta != null) {
            filtros = filtros.and(EventoSpecifications.entreFechas(desde, hasta)
            );
        }
        return repositorio.findAll(filtros, PageRequest.of(pagina, tamano)
        ).map(evento -> new EventoResumenDTO(
                evento.getId(),
                evento.getTitulo(),
                evento.getFechaInicio(),
                evento.getFechaFin(),
                evento.getEstado()
        ));
    }
    // Endpoint 2: calendario agrupado por día
    @GetMapping("/calendario")
    public Map<String, List<EventoCalendarioView>> calendario(
            @RequestParam int mes,
            @RequestParam int anio) {
        List<EventoCalendarioView> eventos = repositorio.calendarioDelMes(mes, anio);
        return eventos.stream() //vamo a recorrer to los evnets pa organiarlos
                        .collect(Collectors.groupingBy( //juntamos events con algo en común
                        EventoCalendarioView::getDia, // we use day of events pa asignarle grupito
                        LinkedHashMap::new, //create structure to save days y events manteniendo el orden que aparecen.
                        Collectors.toList() //to los events que tengan same day lo guardamo to juntos en lista.
                ));
    }
    // Endpoint 3: detalle de un evento
    @GetMapping("/{id}")
    public EventoDetalleDTO detalle(@PathVariable Integer id) {
        return servicio.detalle(id);
    }
    // Endpoint 4: crear un evento
// Pa pruebecias: idOrganizador solo pa test Postman
    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(
            @RequestBody CrearEventoDTO datos,
            @RequestParam Integer idOrganizador) {
        // find organi
        Usuario organizador = usuarioRepository.findById(idOrganizador).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"
                ));
        // Creamos event using el service
        Evento evento = servicio.crear(datos, organizador);
        // El create 201
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "id", evento.getId(),
                        "titulo", evento.getTitulo(),
                        "estado", evento.getEstado().name()
                )
        );
    }
    // Endpoint 5: edit event
    // Pa pruebas: idOrganizador solo pa test Postman
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> editar(
            @PathVariable Integer id,
            @RequestBody EditarEventoDTO datos,
            @RequestParam Integer idOrganizador) {
        // find user edit
        Usuario usuario = usuarioRepository.findById(idOrganizador).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"
                ));
        // edit event using service
        Evento evento = servicio.editar(id, datos, usuario);
        // return event actualizao
        return ResponseEntity.ok(
                Map.of(
                        "id", evento.getId(),
                        "titulo", evento.getTitulo(),
                        "estado", evento.getEstado().name()
                )
        );
    }
    // Endpoint 6: public o cancel event
    // idOrganizador solo pa pruebas Postman
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Map<String, Object>> cambiarEstado(
            @PathVariable Integer id,
            @RequestBody Map<String, EstadoEvento> datos,
            @RequestParam Integer idOrganizador) {
        // find user
        Usuario usuario = usuarioRepository.findById(idOrganizador).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"
                ));
        // chnage state using service
        Evento evento = servicio.cambiarEstado(id, datos.get("estado"), usuario
        );
        return ResponseEntity.ok(
                Map.of(
                        "id", evento.getId(),
                        "titulo", evento.getTitulo(),
                        "estado", evento.getEstado().name()
                )
        );
    }
}

//http://localhost:8080/api/v1/eventos?pagina=0&tamano=5
//0&y eso es pa la paginación
//http://localhost:8080/api/v1/eventos?pagina=1&tamano=2
//pedimo la segunda pag y solo mostramo máx 2 eventos

//confirmar filtrao con http://localhost:8080/api/v1/eventos?texto=Feria

//Buscar eventos con id 1 es http://localhost:8080/api/v1/eventos?categoria=1

//eventos categoria 1 y tenga word feria http://localhost:8080/api/v1/eventos?categoria=1&texto=Feria

//probamos metodo entrefechas in eventospecification: http://localhost:8080/api/v1/eventos?desde=2026-11-01T00:00:00&hasta=2026-11-30T23:59:59

//public events dirigidos al curso 1 y open a all the center: http://localhost:8080/api/v1/eventos?curso=1

//SEGUNDO ENDOPOINT: http://localhost:8080/api/v1/eventos/calendario?mes=11&anio=2026 pa devolver events w correspond mes y año
    //http://localhost:8080/api/v1/eventos/calendario?mes=11&anio=2026 pa sacar dia y agrupar events de se dia
