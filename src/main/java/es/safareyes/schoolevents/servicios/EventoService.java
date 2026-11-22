package es.safareyes.schoolevents.servicios;

import es.safareyes.schoolevents.dto.EventoDetalleDTO;
import es.safareyes.schoolevents.modelos.*;
import es.safareyes.schoolevents.repositorios.EventoRepository;
import es.safareyes.schoolevents.repositorios.InscripcionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import es.safareyes.schoolevents.dto.CrearEventoDTO;
import es.safareyes.schoolevents.repositorios.CategoriaRepository;
import es.safareyes.schoolevents.repositorios.LugarRepository;
import es.safareyes.schoolevents.repositorios.CursoRepository;
import es.safareyes.schoolevents.repositorios.RequisitoRepository;
import java.util.List;

@Service
public class EventoService {
    private final EventoRepository eventoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final CategoriaRepository categoriaRepository;
    private final LugarRepository lugarRepository;
    private final CursoRepository cursoRepository;
    private final RequisitoRepository requisitoRepository;

    public EventoService(
            EventoRepository eventoRepository,
            InscripcionRepository inscripcionRepository,
            CategoriaRepository categoriaRepository,
            LugarRepository lugarRepository,
            CursoRepository cursoRepository,
            RequisitoRepository requisitoRepository) {
        this.eventoRepository = eventoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.categoriaRepository = categoriaRepository;
        this.lugarRepository = lugarRepository;
        this.cursoRepository = cursoRepository;
        this.requisitoRepository = requisitoRepository;
    }
    // ENDPOINT 3: detail of event
    @Transactional(readOnly = true)
    public EventoDetalleDTO detalle(Integer id) {
        // find event x id
        Evento evento = eventoRepository.findConDetalleById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Evento no encontrado"
                ));
        // only show events public or finalizaos
        if (evento.getEstado() != EstadoEvento.PUBLICADO &&
                evento.getEstado() != EstadoEvento.FINALIZADO) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Evento no encontrado"
            );
        }
        // count person tienen plaza
        long ocupadas = inscripcionRepository
                .countByEventoIdAndTipoAndEstado(
                        id,
                        TipoInscripcion.ASISTIRE,
                        EstadoInscripcion.CONFIRMADA
                );
        // Si no have aforo propio, use el del lugar
        Integer aforo = evento.getAforo();
        if (aforo == null && evento.getLugar() != null) {
            aforo = evento.getLugar().getAforo();
        }
        // Calc plazas free
        long plazasLibres = aforo == null ? 0 :
                Math.max(0L, aforo.longValue() - ocupadas);
        // Obten ID de cursos
        List<Integer> cursos = evento.getCursos() == null
                ? List.of()
                : evento.getCursos().stream()
                .map(Curso::getId)
                .toList();
        // Obten ID de requi
        List<Integer> requisitos = evento.getRequisitos() == null
                ? List.of()
                : evento.getRequisitos().stream()
                .map(Requisito::getId)
                .toList();

        // Devolv datos de event
        return new EventoDetalleDTO(
                evento.getId(),
                evento.getTitulo(),
                evento.getDescripcion(),
                evento.getFechaInicio(),
                evento.getFechaFin(),
                evento.getFechaLimite(),
                evento.getEstado(),
                aforo,
                plazasLibres,
                evento.getLugar() == null
                        ? null : evento.getLugar().getNombre(),
                evento.getCategoria() == null
                        ? null : evento.getCategoria().getNombre(),
                cursos,
                requisitos
        );
    }
    //pa el endpoint 4: crear event
    @Transactional
    public Evento crear(CrearEventoDTO datos, Usuario organizador) {
        // o organizador o admin
        if (organizador == null ||
                (organizador.getRol() != RolUsuario.ORGANIZADOR &&
                        organizador.getRol() != RolUsuario.ADMIN)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permisos"
            );
        }
        // test datos obligatory
        if (datos == null ||
                datos.titulo() == null ||
                datos.titulo().isBlank() ||
                datos.fechaInicio() == null ||
                datos.fechaFin() == null ||
                datos.idLugar() == null ||
                datos.idCategoria() == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Faltan datos obligatorios"
            );
        }
        // fecha fin have to posterior a ini
        if (!datos.fechaFin().isAfter(datos.fechaInicio())) {

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "La fecha de fin debe ser posterior al inicio"
            );
        }
        // fecaçha liminte cants superar a la date de ini
        if (datos.fechaLimite() != null &&
                datos.fechaLimite().isAfter(datos.fechaInicio())) {

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Fecha limite incorrecta"
            );
        }
        //find place
        Lugar lugar = lugarRepository.findById(datos.idLugar())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Lugar no encontrado"
                ));
        // find cat
        Categoria categoria = categoriaRepository
                .findById(datos.idCategoria())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Categoria no encontrada"
                ));
        //si dont have aforo propio, we use el del lugar
        Integer aforo = datos.aforo() == null
                ? lugar.getAforo()
                : datos.aforo();
        //test aforo correct
        if (aforo == null ||
                aforo <= 0 ||
                lugar.getAforo() == null ||
                aforo > lugar.getAforo()) {

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Aforo incorrecto"
            );
        }
        // po pa saber si hay event en el mismo lugar y hora
        long solapados = eventoRepository.contarSolapamientos(
                datos.idLugar(),
                datos.fechaInicio(),
                datos.fechaFin(),
                EstadoEvento.CANCELADO
        );
        if (solapados > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un evento en ese lugar y horario"
            );
        }
        //find cursos
        List<Integer> idsCursos = datos.cursos() == null
                ? List.of()
                : datos.cursos().stream()
                .distinct()
                .toList();

        List<Curso> cursos = cursoRepository.findAllById(idsCursos);
        // find reequisi
        List<Integer> idsRequisitos = datos.requisitos() == null
                ? List.of()
                : datos.requisitos().stream()
                .distinct()
                .toList();

        List<Requisito> requisitos =
                requisitoRepository.findAllById(idsRequisitos);

        // test que exist all curses y requisi
        if (cursos.size() != idsCursos.size() ||
                requisitos.size() != idsRequisitos.size()) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Curso o requisito no encontrado"
            );
        }
        Evento evento = new Evento();
        // save datos recived
        evento.setTitulo(datos.titulo());
        evento.setDescripcion(datos.descripcion());
        evento.setFechaInicio(datos.fechaInicio());
        evento.setFechaFin(datos.fechaFin());
        evento.setFechaLimite(datos.fechaLimite());
        evento.setAforo(aforo);
        evento.setLugar(lugar);
        evento.setCategoria(categoria);
        evento.setOrganizador(organizador);
        evento.setCursos(cursos);
        evento.setRequisitos(requisitos);
        // to events start en borrador
        evento.setEstado(EstadoEvento.BORRADOR);
        // save proye in Postgres
        return eventoRepository.save(evento);
    }
}

