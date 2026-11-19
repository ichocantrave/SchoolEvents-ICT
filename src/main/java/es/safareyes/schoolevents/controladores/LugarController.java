package es.safareyes.schoolevents.controladores;

import es.safareyes.schoolevents.modelos.Lugar;
import es.safareyes.schoolevents.repositorios.LugarRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lugares")
public class LugarController {

    private final LugarRepository repositorio;

    public LugarController(LugarRepository repositorio) {
        this.repositorio = repositorio;
    }

    @GetMapping
    public List<Lugar> listar() {
        return repositorio.findAll();
    }
}
//http://localhost:8080/api/v1/lugares