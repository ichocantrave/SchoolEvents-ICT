package es.safareyes.schoolevents;

import es.safareyes.schoolevents.modelos.Evento;
import es.safareyes.schoolevents.repositorios.CategoriaRepository;
import es.safareyes.schoolevents.repositorios.EventoRepository;
import es.safareyes.schoolevents.repositorios.EventoSpecifications;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// las specification son filtros que se pueden juntar con .and())
// Hacemo listado con filtros combinables
@DisplayName("Specification") //Q7
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class Q7SpecificationTest {

    @Autowired
    private EventoRepository eventoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;

    @Test
    @DisplayName("Dos filtros juntos, categoría Cultural y texto 'visita'")//endpoint 1
    void categoriaYTexto() {
        Integer idCultural = categoriaRepository.findByNombre("Cultural").get().getId();

        List<Evento> eventos = eventoRepository.findAll(
                EventoSpecifications.conCategoria(idCultural).and(EventoSpecifications.tituloContiene("visita")));

        System.out.println("Eventos Cultural con 'visita' en el título: " + eventos.size()); //Q7
        // Solo el Real Alcázar y el Barrio de Triana
        assertEquals(2, eventos.size());
    }
}
