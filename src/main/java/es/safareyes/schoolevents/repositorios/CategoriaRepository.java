package es.safareyes.schoolevents.repositorios;

import es.safareyes.schoolevents.modelos.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    Optional<Categoria> findByNombre(String nombre);
    //Declara un metodo Dato Spring JPA del repositorio que busca categoria por su nombre del campo.
}
