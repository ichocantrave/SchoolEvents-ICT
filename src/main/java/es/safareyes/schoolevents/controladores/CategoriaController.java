package es.safareyes.schoolevents.controladores;

import es.safareyes.schoolevents.modelos.Categoria;
import es.safareyes.schoolevents.repositorios.CategoriaRepository;
import org.springframework.web.bind.annotation.*; //pa las anotaciones de spring we need to crear endpoints

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
public class CategoriaController {

    private final CategoriaRepository repositorio;

    public CategoriaController(CategoriaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @GetMapping
    public List<Categoria> listar() {
        return repositorio.findAll();
    }
}
//http://localhost:8080/api/v1/categorias