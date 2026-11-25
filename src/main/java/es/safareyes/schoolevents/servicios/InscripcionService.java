package es.safareyes.schoolevents.servicios;

import es.safareyes.schoolevents.modelos.*;
import es.safareyes.schoolevents.repositorios.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.List;

@Service
public class InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionRepository notificacionRepository;
    private final UsuarioNotificacionRepository usuarioNotificacionRepository;

    public InscripcionService(
            InscripcionRepository inscripcionRepository,
            EventoRepository eventoRepository,
            UsuarioRepository usuarioRepository,
            NotificacionRepository notificacionRepository,
            UsuarioNotificacionRepository usuarioNotificacionRepository) {

        this.inscripcionRepository = inscripcionRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacionRepository = notificacionRepository;
        this.usuarioNotificacionRepository = usuarioNotificacionRepository;
    }

    //Endpoint 9: Inscrib a event
    @Transactional
    public Inscripcion inscribir(Integer idEvento, Integer idUsuario, TipoInscripcion tipo){
        //find event
        Evento evento = eventoRepository.findById(idEvento).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Evento no encontrado"
                ));
        //find user
        Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"
        ));
        //solo students
        if(usuario.getRol() != RolUsuario.ESTUDIANTE){
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,"Solo los estudiantes pueden inscribirse");
        }
        //test tipo
        if(tipo==null){
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT, "Tipo de inscripcion obligatorio"
            );
        }
        //solo evnts publicaos
        if(evento.getEstado()!=EstadoEvento.PUBLICADO){
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT,"El evento no admite inscripciones"
            );
        }
        //test fecha limite
        if(evento.getFechaLimite() !=null && LocalDateTime.now().isAfter(evento.getFechaLimite())){
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT, "Ha terminado el plazo de inscripcióon"
            );

        }
        //test event no start
        if(!LocalDateTime.now().isBefore(evento.getFechaInicio())){
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT, "El evento ya ha comenzado"
            );
        }
        //test cursos allow
        if (!evento.getCursos().isEmpty()) {

            if (usuario.getCurso() == null || evento.getCursos().stream().noneMatch(curso -> curso.getId().equals(usuario.getCurso().getId()))) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Este evento no es para tu curso");
            }
        }
        //find if ya ta iscrito
        Inscripcion inscripcion = inscripcionRepository.findByUsuarioIdAndEventoId(idUsuario, idEvento).orElse(new Inscripcion());
        //si ya have same tipo dont change na
        if (inscripcion.getId()!=null && inscripcion.getTipo() == tipo){
            return inscripcion;
        }
        inscripcion.setUsuario(usuario);
        inscripcion.setEvento(evento);
        inscripcion.setTipo(tipo);

        //Me interesa no ocupa plaza
        if (tipo == TipoInscripcion.ME_INTERESA) {
            inscripcion.setEstado(EstadoInscripcion.CONFIRMADA);
        }else{
            //contar plazas ocupadas
            long ocupadas = inscripcionRepository. countByEventoIdAndTipoAndEstado(idEvento,TipoInscripcion.ASISTIRE, EstadoInscripcion.CONFIRMADA);
            //comproba aforo
            Integer aforo  = evento.getAforo() == null? evento.getLugar().getAforo() : evento.getAforo();
            if (aforo == null|| aforo<= 0){
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_CONTENT, "Aforo incorrecto"
                );
            }
            if (ocupadas < aforo ){
                inscripcion.setEstado(EstadoInscripcion.CONFIRMADA);
            } else {
                inscripcion.setEstado(EstadoInscripcion.EN_ESPERA);
            }
        }
        //po pa save la inscrip
        return inscripcionRepository.save(inscripcion);
    }

    // ENDPOINT 10: delet inscrip
    @Transactional
    public void borrarInscripcion(Integer idEvento, Integer idUsuario) {
        // find event
        Evento evento = eventoRepository.findById(idEvento).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Evento no encontrado"
        ));
        // find user
        Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"
        ));

        // solo students
        if (usuario.getRol() != RolUsuario.ESTUDIANTE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permisos"
            );
        }
        // find inscripc
        Inscripcion inscripcion = inscripcionRepository.findByUsuarioIdAndEventoId(idUsuario, idEvento)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Inscripcion no encontrada"));
        // test if ocuapa plaza
        boolean teniaPlaza = inscripcion.getTipo() == TipoInscripcion.ASISTIRE &&
                        inscripcion.getEstado() == EstadoInscripcion.CONFIRMADA;
        // delet inscrip
        inscripcionRepository.delete(inscripcion);
        // si no ocupaba plaza no hace falta promocionar
        if (!teniaPlaza) {
            return;
        }
        // find 1º list de wait
        Inscripcion siguiente = inscripcionRepository.findFirstByEventoIdAndEstadoOrderByCreadaEnAsc(
                        idEvento, EstadoInscripcion.EN_ESPERA).orElse(null);
        if (siguiente == null) {
            return;
        }
        // darle la plaza
        siguiente.setEstado(EstadoInscripcion.CONFIRMADA);
        inscripcionRepository.save(siguiente);
        // create notifi
        Notificacion notificacion = new Notificacion();
        notificacion.setMensaje(
                "Has conseguido una plaza en el evento '" +
                        evento.getTitulo() + "'"
        );
        notificacionRepository.save(notificacion);
        // send aviso user
        UsuarioNotificacion aviso = new UsuarioNotificacion();
        aviso.setUsuario(siguiente.getUsuario());
        aviso.setNotificacion(notificacion);
        aviso.setLeida(false);
        usuarioNotificacionRepository.save(aviso);
    }
    //Endpoint 11: mi agend de events
    @Transactional(readOnly = true)
    public List<Map<String, Object>> miAgenda(Integer idUsuario, TipoInscripcion tipo){
        //find user
        Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow(()-> new ResponseStatusException(
                HttpStatus.NOT_FOUND,"Usuario no encontrado"
        ));
        // solo students
        if (usuario.getRol() != RolUsuario.ESTUDIANTE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permisos"
            );
        }
        // test tipo
        if (tipo == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "Tipo de inscripcion obligatorio"
            );
        }
        // find mis inscrip
        List<Inscripcion> inscripciones =
                inscripcionRepository.findByUsuarioIdAndTipo(idUsuario, tipo);
        // devolver los event
        return inscripciones.stream()
                .map(inscripcion -> {
                    Evento evento = inscripcion.getEvento();
                    Map<String, Object> datos = new LinkedHashMap<>();
                    datos.put("id", evento.getId());
                    datos.put("titulo", evento.getTitulo());
                    datos.put("fechaInicio", evento.getFechaInicio());
                    datos.put("fechaFin", evento.getFechaFin());
                    datos.put("tipo", inscripcion.getTipo());
                    datos.put("estadoInscripcion", inscripcion.getEstado());
                    return datos;
                })
                .toList();
    }
    //Endpoint 12: listar inscrip w paginaci
    @Transactional(readOnly = true)
    public Page<Map<String, Object>> listarInscritos( Integer idEvento, Integer idUsuario, int pagina, int tamano) {
        //find event
        Evento evento = eventoRepository.findById(idEvento).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Usuario no encontrado"
        ));
        //find user
        Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "No encontrao el usuario"
        ));
        //test permisos
        if (usuario.getRol() != RolUsuario.ADMIN && (usuario.getRol() != RolUsuario.ORGANIZADOR || evento.getOrganizador()
                == null || !evento.getOrganizador().getId().equals(usuario.getId()))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permisos"
            );
        }
        //test paginacion
        if (pagina < 0 || tamano < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "paginacion incorrecta"
            );
        }
        //find inscrip
        Page<Inscripcion> inscripciones = inscripcionRepository.findByEventoId(idEvento, PageRequest.of(pagina, tamano)
        );
        //devolver datos
        return inscripciones.map(inscripcion -> {
            Map<String, Object> datos = new LinkedHashMap<>();
            datos.put("idInscripcion", inscripcion.getId());
            datos.put("idUsuario", inscripcion.getUsuario().getId());
            datos.put("nombre", inscripcion.getUsuario().getNombre());
            datos.put("tipo", inscripcion.getTipo());
            datos.put("estado", inscripcion.getEstado());
            return datos;

        });
    }
    //endpoint 13: pasar list de asistencia
    @Transactional
    public Inscripcion marcarAsistencia(Integer idEvento, Integer idInscripcion, Integer idUsuario, Boolean asistio) {
        // find event
        Evento evento = eventoRepository.findById(idEvento).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Evento no encontrado"
                ));
        // find user
        Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"
                ));
        // test permis
        if (usuario.getRol() != RolUsuario.ADMIN && (usuario.getRol() != RolUsuario.ORGANIZADOR ||
                        evento.getOrganizador() == null || !evento.getOrganizador().getId().equals(usuario.getId()))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permisos"
            );
        }
        // find inscrip
        Inscripcion inscripcion = inscripcionRepository.findById(idInscripcion).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Inscripcion no encontrada"
                ));
        // test que la inscrip perten al event
        if (!inscripcion.getEvento().getId().equals(idEvento)) {throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Inscripcion no encontrada en este evento"
            );
        }
        // solo alumn w plaza confirm
        if (inscripcion.getTipo() != TipoInscripcion.ASISTIRE || inscripcion.getEstado() != EstadoInscripcion.CONFIRMADA) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT, "El alumno no tiene plaza confirmada"
            );
        }
        // test dato obligatorio
        if (asistio == null) {throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT, "Debes indicar la asistencia"
            );
        }
        // save asist
        inscripcion.setAsistio(asistio);
        return inscripcionRepository.save(inscripcion);
    }
}
