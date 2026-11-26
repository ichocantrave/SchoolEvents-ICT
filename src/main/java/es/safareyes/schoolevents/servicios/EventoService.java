package es.safareyes.schoolevents.servicios;

import es.safareyes.schoolevents.dto.CrearEventoDTO;
import es.safareyes.schoolevents.dto.EditarEventoDTO;
import es.safareyes.schoolevents.dto.EventoDetalleDTO;
import es.safareyes.schoolevents.modelos.*;
import es.safareyes.schoolevents.repositorios.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final CategoriaRepository categoriaRepository;
    private final LugarRepository lugarRepository;
    private final CursoRepository cursoRepository;
    private final RequisitoRepository requisitoRepository;
    private final CambioEventoRepository cambioEventoRepository;
    private final NotificacionRepository notificacionRepository;
    private final UsuarioNotificacionRepository usuarioNotificacionRepository;

    public EventoService(
            EventoRepository eventoRepository,
            InscripcionRepository inscripcionRepository,
            CategoriaRepository categoriaRepository,
            LugarRepository lugarRepository,
            CursoRepository cursoRepository,
            RequisitoRepository requisitoRepository,
            CambioEventoRepository cambioEventoRepository,
            NotificacionRepository notificacionRepository,
            UsuarioNotificacionRepository usuarioNotificacionRepository) {

        this.eventoRepository = eventoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.categoriaRepository = categoriaRepository;
        this.lugarRepository = lugarRepository;
        this.cursoRepository = cursoRepository;
        this.requisitoRepository = requisitoRepository;
        this.cambioEventoRepository = cambioEventoRepository;
        this.notificacionRepository = notificacionRepository;
        this.usuarioNotificacionRepository = usuarioNotificacionRepository;
    }

    // ENDPOINT 3: detail of event
    @Transactional(readOnly = true)
    public EventoDetalleDTO detalle(Integer id) {
        // find event x id
        Evento evento = eventoRepository.findConDetalleById(id).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Evento no encontrado"
                ));
        // only show events public or finalizaos
        if (evento.getEstado() != EstadoEvento.PUBLICADO && evento.getEstado() != EstadoEvento.FINALIZADO) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Evento no encontrado"
            );
        }
        // count person tienen plaza
        long ocupadas = inscripcionRepository.countByEventoIdAndTipoAndEstado(
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
        long plazasLibres = aforo == null ? 0 : Math.max(0L, aforo.longValue() - ocupadas);
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
        if (organizador == null || (organizador.getRol() != RolUsuario.ORGANIZADOR && organizador.getRol() != RolUsuario.ADMIN)) {
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
                    HttpStatus.UNPROCESSABLE_CONTENT,"Faltan datos obligatorios"
            );
        }
        // fecha fin have to posterior a ini
        if (!datos.fechaFin().isAfter(datos.fechaInicio())) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT,"La fecha de fin debe ser posterior al inicio"
            );
        }
        // fecaçha liminte cants superar a la date de ini
        if (datos.fechaLimite() != null &&
                datos.fechaLimite().isAfter(datos.fechaInicio())) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT,"Fecha limite incorrecta"
            );
        }
        //find place
        Lugar lugar = lugarRepository.findById(datos.idLugar()).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Lugar no encontrado"
                ));
        // find cat
        Categoria categoria = categoriaRepository.findById(datos.idCategoria()).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Categoria no encontrada"
                ));
        //si dont have aforo propio, we use el del lugar
        Integer aforo = datos.aforo() == null
                ? lugar.getAforo()
                : datos.aforo();
        //test aforo correct
        if (aforo == null || aforo <= 0 || lugar.getAforo() == null || aforo > lugar.getAforo()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT, "Aforo incorrecto"
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
                    HttpStatus.CONFLICT, "Ya existe un evento en ese lugar y horario"
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
        if (cursos.size() != idsCursos.size() || requisitos.size() != idsRequisitos.size()) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Curso o requisito no encontrado"
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

    // ENDPOINT 5: edit event
    @Transactional
    public Evento editar(Integer id, EditarEventoDTO datos, Usuario usuario) {
        // find evento
        Evento evento = eventoRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Evento no encontrado"
                ));
        // test permisos
        if (usuario == null || (usuario.getRol() != RolUsuario.ADMIN && (usuario.getRol() != RolUsuario.ORGANIZADOR || evento.getOrganizador() == null ||
                !evento.getOrganizador().getId().equals(usuario.getId())))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos");
        }
        // no editar eventos finalizados o cancelados
        if (evento.getEstado() == EstadoEvento.FINALIZADO || evento.getEstado() == EstadoEvento.CANCELADO) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT, "No se puede editar un evento finalizado o cancelado"
            );
        }
        // test datos obligatorios
        if (datos == null ||
                datos.titulo() == null || datos.titulo().isBlank() ||
                datos.fechaInicio() == null ||
                datos.fechaFin() == null ||
                datos.idLugar() == null ||
                datos.idCategoria() == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT, "Faltan datos obligatorios"
            );
        }
        // test fechas
        if (!datos.fechaFin().isAfter(datos.fechaInicio()) ||
                (datos.fechaLimite() != null && datos.fechaLimite().isAfter(datos.fechaInicio()))) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT, "Fechas incorrectas"
            );
        }
        // find lugar y categoria
        Lugar lugar = lugarRepository.findById(datos.idLugar()).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Lugar no encontrado"
                ));
        Categoria categoria = categoriaRepository.findById(datos.idCategoria()).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Categoria no encontrada"
                ));
        // test aforo
        Integer aforo = datos.aforo() == null
                ? lugar.getAforo() : datos.aforo();
        long ocupadas = inscripcionRepository
                .countByEventoIdAndTipoAndEstado(
                        id,
                        TipoInscripcion.ASISTIRE,
                        EstadoInscripcion.CONFIRMADA
                );
        if (aforo == null || aforo <= 0 ||
                lugar.getAforo() == null ||
                aforo > lugar.getAforo() ||
                aforo < ocupadas) {

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT, "Aforo incorrecto"
            );
        }
        // test solapamientos sin contar el propio evento
        long solapados = eventoRepository.contarSolapamientosAlEditar(
                id,
                datos.idLugar(),
                datos.fechaInicio(),
                datos.fechaFin(),
                EstadoEvento.CANCELADO
        );
        if (solapados > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe un evento en ese lugar y horario"
            );
        }
        // find cursos
        List<Integer> idsCursos = datos.cursos() == null
                ? List.of()
                : datos.cursos().stream().distinct().toList();
        List<Curso> cursos = cursoRepository.findAllById(idsCursos);
        // find requisitos
        List<Integer> idsRequisitos = datos.requisitos() == null
                ? List.of()
                : datos.requisitos().stream().distinct().toList();
        List<Requisito> requisitos = requisitoRepository.findAllById(idsRequisitos);
        if (cursos.size() != idsCursos.size() || requisitos.size() != idsRequisitos.size()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Curso o requisito no encontrado"
            );
        }
        // if event is pblicao, save change
        if (evento.getEstado() == EstadoEvento.PUBLICADO) {
            guardarCambio(
                    evento, usuario, "fechaInicio",
                    evento.getFechaInicio(), datos.fechaInicio()
            );
            guardarCambio(
                    evento, usuario, "fechaFin",
                    evento.getFechaFin(), datos.fechaFin()
            );
            guardarCambio(
                    evento, usuario, "lugar",
                    evento.getLugar() == null
                            ? null : evento.getLugar().getId(),
                    lugar.getId()
            );
            // notify si change date o place
            boolean hayCambios =
                    !Objects.equals(evento.getFechaInicio(), datos.fechaInicio()) ||
                            !Objects.equals(evento.getFechaFin(), datos.fechaFin()) ||
                            !Objects.equals(
                                    evento.getLugar() == null
                                            ? null : evento.getLugar().getId(),
                                    lugar.getId()
                            );
            if (hayCambios) {
                notificarCambio(evento);
            }
        }
        // actuali event
        evento.setTitulo(datos.titulo());
        evento.setDescripcion(datos.descripcion());
        evento.setFechaInicio(datos.fechaInicio());
        evento.setFechaFin(datos.fechaFin());
        evento.setFechaLimite(datos.fechaLimite());
        evento.setAforo(aforo);
        evento.setLugar(lugar);
        evento.setCategoria(categoria);
        evento.setCursos(cursos);
        evento.setRequisitos(requisitos);
        // save datos
        return eventoRepository.save(evento);
    }
    // Save change de event
    private void guardarCambio(Evento evento, Usuario usuario, String campo, Object anterior, Object nuevo) {
        // test if valor change
        if (!Objects.equals(anterior, nuevo)) {
            CambioEvento cambio = new CambioEvento();
            cambio.setEvento(evento);
            cambio.setAutor(usuario);
            cambio.setCampo(campo);
            cambio.setValorAnterior(String.valueOf(anterior));
            cambio.setValorNuevo(String.valueOf(nuevo));
            cambio.setFechaCambio(LocalDateTime.now());
            cambioEventoRepository.save(cambio);
        }
    }
    // ENDPOINT 5: notif chnage a los inscritos
    private void notificarCambio(Evento evento) {
        // find inscripciones del evento
        List<Inscripcion> inscripciones =
                inscripcionRepository.findByEventoId(evento.getId());
        // get user recived noti
        List<Usuario> usuarios = inscripciones.stream()
                .map(Inscripcion::getUsuario)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        // po si no hay usuarios po no creamos la notificacion
        if (usuarios.isEmpty()) {
            return;
        }
        // create noti
        Notificacion notificacion = new Notificacion();
        notificacion.setMensaje("El evento '" + evento.getTitulo() + "' ha cambiado de fecha, hora o lugar"
        );
        notificacionRepository.save(notificacion);
        // noti a each user
        for (Usuario usuario : usuarios) {
            UsuarioNotificacion aviso = new UsuarioNotificacion();
            aviso.setUsuario(usuario);
            aviso.setNotificacion(notificacion);
            aviso.setLeida(false);
            usuarioNotificacionRepository.save(aviso);
        }
    }
    // ENDPOINT 6: public o cancel event
    @Transactional
    public Evento cambiarEstado(Integer id, EstadoEvento nuevoEstado, Usuario usuario) {
        // find evento
        Evento evento = eventoRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Evento no encontrado"
                ));
        // test permisos
        if (usuario == null ||
                (usuario.getRol() != RolUsuario.ADMIN && (usuario.getRol() != RolUsuario.ORGANIZADOR || evento.getOrganizador() == null ||
                                !evento.getOrganizador().getId().equals(usuario.getId())))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permisos"
            );
        }
        // public only si es borraod
        if (nuevoEstado == EstadoEvento.PUBLICADO &&
                evento.getEstado() == EstadoEvento.BORRADOR) {
            // solapamientos?
            long solapados = eventoRepository.contarSolapamientosAlEditar(
                    id,
                    evento.getLugar().getId(),
                    evento.getFechaInicio(),
                    evento.getFechaFin(),
                    EstadoEvento.CANCELADO
            );
            if (solapados > 0) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Ya existe un evento en ese lugar y horario"
                );
            }
            evento.setEstado(EstadoEvento.PUBLICADO);
            // cancel desde Draft or public
        } else if (nuevoEstado == EstadoEvento.CANCELADO && (evento.getEstado() == EstadoEvento.BORRADOR || evento.getEstado() == EstadoEvento.PUBLICADO)) {
            evento.setEstado(EstadoEvento.CANCELADO);
            notificarCancelacion(evento); // advice a los inscri
        } else {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT, "Cambio de estado no permitido"
            );
        }
        // save estado
        return eventoRepository.save(evento);
    }
    // ENDPOINT 6: notify cancelacion
    private void notificarCancelacion(Evento evento) {
        List<Inscripcion> inscripciones = inscripcionRepository.findByEventoId(evento.getId());
        if (inscripciones.isEmpty()) {
            return;
        }
        // create notifi
        Notificacion notificacion = new Notificacion();
        notificacion.setMensaje("El evento '" + evento.getTitulo() + "' ha sido cancelado");
        notificacionRepository.save(notificacion);
        // notifi a tos los inscri
        for (Inscripcion inscripcion : inscripciones) {
            UsuarioNotificacion aviso = new UsuarioNotificacion();
            aviso.setUsuario(inscripcion.getUsuario());
            aviso.setNotificacion(notificacion);
            aviso.setLeida(false);
            usuarioNotificacionRepository.save(aviso);
        }
    }

}
