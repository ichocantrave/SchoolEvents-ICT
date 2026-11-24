package es.safareyes.schoolevents.servicios;

import es.safareyes.schoolevents.modelos.*;
import es.safareyes.schoolevents.repositorios.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
private final NotificacionRepository notificacionRepository;
private final UsuarioNotificacionRepository usuarioNotificacionRepository;

@Service
public class InscripcionService {
    private final InscripcionRepository inscripcionRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;


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

}
