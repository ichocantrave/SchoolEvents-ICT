package es.safareyes.schoolevents.servicios;

import es.safareyes.schoolevents.dto.ParticipacionView;
import es.safareyes.schoolevents.modelos.*;
import es.safareyes.schoolevents.repositorios.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InformeService {
    private final InscripcionRepository inscripcionRepository;
    private final UsuarioRepository usuarioRepository;

    public InformeService(
            InscripcionRepository inscripcionRepository,
            UsuarioRepository usuarioRepository) {
        this.inscripcionRepository = inscripcionRepository;
        this.usuarioRepository = usuarioRepository;
    }
    // ENDPOINT 16: inform partic
    @Transactional(readOnly = true)
    public List<ParticipacionView> participacion(
            Integer idUsuario,
            LocalDateTime desde,
            LocalDateTime hasta) {

        // find user
        Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"
                ));
        // solo admin
        if (usuario.getRol() != RolUsuario.ADMIN) {throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permisos"
            );
        }
        // test fechas
        if (desde == null || hasta == null || desde.isAfter(hasta)) {throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Fechas incorrectas"
            );
        }
        // obtain inform
        return inscripcionRepository.informeParticipacion(desde, hasta);
    }
}
