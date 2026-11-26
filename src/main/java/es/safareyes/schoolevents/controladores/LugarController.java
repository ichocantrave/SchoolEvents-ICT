package es.safareyes.schoolevents.controladores;

import es.safareyes.schoolevents.modelos.Lugar;
import es.safareyes.schoolevents.modelos.Usuario;
import es.safareyes.schoolevents.modelos.RolUsuario;
import es.safareyes.schoolevents.repositorios.LugarRepository;
import es.safareyes.schoolevents.repositorios.UsuarioRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lugares")
public class LugarController {

    private final LugarRepository repositorio;
    private final UsuarioRepository usuarioRepository;

    public LugarController(LugarRepository repositorio, UsuarioRepository usuarioRepository) {
        this.repositorio = repositorio;
        this.usuarioRepository = usuarioRepository;
    }
    //ENDPOINT 8: consult places
    @GetMapping
    public List<Lugar> listar(@RequestParam Integer idUsuario) {
        //find user
        Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow(()-> new ResponseStatusException(
                HttpStatus.NOT_FOUND,"Usuario no encontrao"
        ));
        //TEst permisss
        if(usuario.getRol() != RolUsuario.ORGANIZADOR && usuario.getRol() != RolUsuario.ADMIN){
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permisos"
            );
        }
        return repositorio.findAll();
    }
}
//http://localhost:8080/api/v1/lugares