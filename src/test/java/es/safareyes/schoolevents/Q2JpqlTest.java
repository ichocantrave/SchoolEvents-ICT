package es.safareyes.schoolevents;

import es.safareyes.schoolevents.modelos.Evento;
import es.safareyes.schoolevents.repositorios.EventoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Q2 - Consultas JPQL (con @Query, escritas con el nombre de las clases)
// filtramos eventos por categorias
@DisplayName("Consultas JPQL") //Q2
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class Q2JpqlTest {

    @Autowired
    private EventoRepository eventoRepository;

    @Test
    @DisplayName("Eventos de la categoría Cultural")//endpoint 1
    void eventosCulturales() {
        List<Evento> eventos = eventoRepository.buscarPorCategoria("Cultural");

        System.out.println("Eventos de la categoría Cultural: " + eventos.size());
        // Hay 4 eventos culturales y el más antiguo es el de Triana y olé
        assertEquals(4, eventos.size());
        assertEquals("Visita Barrio de Triana", eventos.get(0).getTitulo());
    }
}
