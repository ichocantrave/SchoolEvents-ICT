package es.safareyes.schoolevents.servicios;

import es.safareyes.schoolevents.modelos.*;
import es.safareyes.schoolevents.repositorios.*;
import org.hibernate.grammars.hql.HqlParser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
public class PublicacionService {
    private final PublicacionRepository publicacionRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final InscripcionRepository inscripcionRepository;

    public PublicacionService(
            PublicacionRepository publicacionRepository,
            EventoRepository eventoRepository,
            UsuarioRepository usuarioRepository,
            InscripcionRepository inscripcionRepository) {
        this.publicacionRepository = publicacionRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.inscripcionRepository = inscripcionRepository;
    }
    //Endpoint 14: public results
    @Transactional
    public Publicacion crear(Integer idEvento, Integer idUsuario, String texto) {
        //find event
        Evento evento = eventoRepository.findById(idEvento).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Eevento no encontrado"
        ));
        //find user
        Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Usuario no encontrado"
        ));
        // solo eventos finalizados
        if (evento.getEstado() != EstadoEvento.FINALIZADO) {throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT, "El evento no esta finalizado"
            );
        }
        // test texto
        if (texto == null || texto.isBlank() || texto.length() > 2000) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT, "El texto debe tener entre 1 y 2000 caracteres"
            );
        }
        // test si es organiza
        boolean organizador = usuario.getRol() == RolUsuario.ORGANIZADOR && evento.getOrganizador() != null &&
                        evento.getOrganizador().getId().equals(usuario.getId());
        // test si es admin
        boolean admin = usuario.getRol() == RolUsuario.ADMIN;
        // test si es estudiante que asist
        boolean estudianteAsistente = false;
        if (usuario.getRol() == RolUsuario.ESTUDIANTE) {
            Inscripcion inscripcion = inscripcionRepository.findByUsuarioIdAndEventoId(idUsuario, idEvento).orElse(null);
            estudianteAsistente = inscripcion != null &&
                    inscripcion.getTipo() == TipoInscripcion.ASISTIRE && inscripcion.getEstado() == EstadoInscripcion.CONFIRMADA &&
                    Boolean.TRUE.equals(inscripcion.getAsistio()); // debe tene su aasist como true no solo haberse apuntao
        }
        // test permiss
        if (!organizador && !admin && !estudianteAsistente) {throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permisos para publicar resultados"
            );
        }
        // crear publi
        Publicacion publicacion = new Publicacion();
        publicacion.setEvento(evento);
        publicacion.setUsuario(usuario);
        publicacion.setTexto(texto);
        // save publi
        return publicacionRepository.save(publicacion);
    }
    //Endpoint 15: consult public w fotos ago
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarPublicaciones(Integer idEvento){
        //find event
        eventoRepository.findById(idEvento).orElseThrow(()->new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Evento no encontrado"
        ));
        //find public w fotos
        List<Publicacion> publicaciones = publicacionRepository.findByEventoId(idEvento);
        //devolver publi
        return publicaciones.stream().map(publicacion -> {
            Map<String, Object> datos = new LinkedHashMap<>();
            datos.put("id", publicacion.getId());
            datos.put("texto", publicacion.getTexto());
            datos.put("fecha", publicacion.getFecha());
            datos.put("idUsuario", publicacion.getUsuario().getId());

            //obtein fotos of publi
            List<Map<String, Object >> fotos = publicacion.getFotos().stream().map(foto->{
                Map<String, Object> datosFoto = new LinkedHashMap<>();
                datosFoto.put("id", foto.getId());
                datosFoto.put("ruta",foto.getRuta());
                datosFoto.put("descripcion", foto.getDescripcion());
                return datosFoto;
            })
                    .toList();
            datos.put("fotos", fotos);
            return datos;
        }).toList();
    }
}