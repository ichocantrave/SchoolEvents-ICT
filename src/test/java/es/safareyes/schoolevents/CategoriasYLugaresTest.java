package es.safareyes.schoolevents;

import es.safareyes.schoolevents.modelos.Categoria;
import es.safareyes.schoolevents.modelos.Lugar;
import es.safareyes.schoolevents.repositorios.CategoriaRepository;
import es.safareyes.schoolevents.repositorios.LugarRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Endpoints Get que habia que poner las cat con icono y er color y lugares y aforos
// Usan findAll(), que ya viene incluido en JpaRepository por lo tanto me ahorro escribirlo jijiji.
@DisplayName("Categorías y lugares")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//como que springboot no crea o subtituye el test sino que usa la configuracion de la base.
class CategoriasYLugaresTest {

    @Autowired //conecta con campo, paametro o metodo con un bean gestionado por Spring.
    private CategoriaRepository categoriaRepository;
    @Autowired
    private LugarRepository lugarRepository;

    @Test
    @DisplayName("Hay 4 categorías")
    void categorias() {
        List<Categoria> categorias = categoriaRepository.findAll();

        System.out.println("Categorías encontradas: " + categorias.size());
        assertEquals(4, categorias.size());
        // el asserequals es un metodo de JUNIT usado pa test pa comprobar un valor es igual al esperao.
    }

    @Test
    @DisplayName("La categoría Religioso tiene su icono y su color")
    void categoriaReligioso() {
        Categoria religioso = categoriaRepository.findByNombre("Religioso").get();

        System.out.println("Religioso: icono " + religioso.getIcono() + ", color " + religioso.getColor());
        assertEquals("icons/religioso.png", religioso.getIcono());
        assertEquals("#6C3FA0", religioso.getColor());
    }

    @Test
    @DisplayName("Hay 11 lugares")
    void lugares() {
        List<Lugar> lugares = lugarRepository.findAll();

        System.out.println("Lugares encontrados: " + lugares.size());
        assertEquals(11, lugares.size());
    }
}

