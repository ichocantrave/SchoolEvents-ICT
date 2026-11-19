package es.safareyes.schoolevents;

import es.safareyes.schoolevents.modelos.*;
import es.safareyes.schoolevents.repositorios.EventoRepository;
import es.safareyes.schoolevents.repositorios.InscripcionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;

// LA PAGINACIÓN (los resultados se devuelven por páginas)
// 1 listao de eventos y 12 inscritos de un evento
// PageRequest.of(0, 4, ...) significa: página 0 (la primera), de 4 en 4
@DisplayName("Paginación")//Q6
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class Q6PaginacionTest {

    @Autowired
    private EventoRepository eventoRepository;
    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Test
    @DisplayName("10 eventos publicados en páginas de 4")//endpoint 1
    void paginasDeEventos() {
        // Primera página (0) y tercera página (2)
        Page<Evento> primera = eventoRepository.findByEstado(EstadoEvento.PUBLICADO,
                PageRequest.of(0, 4, Sort.by("fechaInicio")));
        Page<Evento> tercera = eventoRepository.findByEstado(EstadoEvento.PUBLICADO,
                PageRequest.of(2, 4, Sort.by("fechaInicio")));

        System.out.println("Para el endpoint 1- " + primera.getTotalElements() + " eventos publicados en " + primera.getTotalPages() + " páginas");
        // En total hay 10 eventos, que son 3 páginas (4 + 4 + 2)
        assertEquals(10, primera.getTotalElements());
        assertEquals(3, primera.getTotalPages());
        assertEquals(4, primera.getContent().size());
        assertEquals(2, tercera.getContent().size());
    }

    @Test
    @DisplayName("6 inscritos del estadio en páginas de 4, con la lista de espera al final") //Endpoint 12
    void paginasDeInscritos() {
        Evento estadio = eventoRepository.findByTituloContainingIgnoreCase("estadio").get(0);

        Page<Inscripcion> primera = inscripcionRepository.findByEventoId(estadio.getId(),
                PageRequest.of(0, 4, Sort.by("creadaEn")));
        Page<Inscripcion> segunda = inscripcionRepository.findByEventoId(estadio.getId(),
                PageRequest.of(1, 4, Sort.by("creadaEn")));

        System.out.println("Para el endpoint 12- " + primera.getTotalElements() + " inscritos en " + primera.getTotalPages() + " páginas");
        // 6 inscritos = una página de 4 y otra de 2
        assertEquals(6, primera.getTotalElements());
        assertEquals(2, primera.getTotalPages());
        assertEquals(4, primera.getContent().size());
        assertEquals(2, segunda.getContent().size());
        // El primero tiene plaza y el de la segunda página está en espera
        assertEquals(EstadoInscripcion.CONFIRMADA, primera.getContent().get(0).getEstado());
        assertEquals(EstadoInscripcion.EN_ESPERA, segunda.getContent().get(0).getEstado());
    }
}
