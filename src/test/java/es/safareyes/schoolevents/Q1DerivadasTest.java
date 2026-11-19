package es.safareyes.schoolevents;

import es.safareyes.schoolevents.modelos.*;
import es.safareyes.schoolevents.repositorios.EventoRepository;
import es.safareyes.schoolevents.repositorios.InscripcionRepository;
import es.safareyes.schoolevents.repositorios.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// Q1 eran las  consultas DERIVADAS
@DisplayName("Consultas derivadas")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class Q1DerivadasTest {

    @Autowired
    private EventoRepository eventoRepository;
    @Autowired
    private InscripcionRepository inscripcionRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("eventos PUBLICADOS ordenados por fecha")
    //esto es pa el endpoint 1
    void eventosPublicados() {
        // Pedimos los eventos publicaos
        List<Evento> eventos = eventoRepository.findByEstadoOrderByFechaInicioAsc(EstadoEvento.PUBLICADO);

        // Comprobamo el mas proximo
        assertEquals(10, eventos.size());
        assertEquals("CICA", eventos.get(0).getTitulo());
        System.out.println("Total de eventos publicados: " + eventos.size());
        System.out.println("El evento más próximo es: " + eventos.get(0).getTitulo());
    }

   // @Test
   //@DisplayName("RF-01: eventos PUBLICADOS entre el 1 de noviembre y el 31 de diciembre de 2026")
   //void eventosEnUnRango() {
   //LocalDateTime desde = LocalDateTime.of(2026, 11, 1, 0, 0);
    //LocalDateTime hasta = LocalDateTime.of(2026, 12, 31, 23, 59);

    //List<Evento> eventos = eventoRepository.findByEstadoAndFechaInicioBetweenOrderByFechaInicioAsc(
    // EstadoEvento.PUBLICADO, desde, hasta);

    //System.out.println("===== RF-01: eventos publicados entre " + desde + " y " + hasta + " =====");
    //System.out.println("Total encontrados: " + eventos.size());
    // for (Evento e : eventos) {
    //System.out.println(" - " + e.getFechaInicio() + " | " + e.getTitulo());
    //}

        // Hay 4 eventos: CICA, estadio, Casa de la Ciencia e IMSE-CNM
        //assertEquals(4, eventos.size());
        //assertEquals("CICA", eventos.get(0).getTitulo());
    //assertEquals("IMSE-CNM", eventos.get(3).getTitulo());
    // }

    @Test
    @DisplayName("RF-02: buscar eventos por texto en el título")
    void buscarPorTitulo() {
        List<Evento> conVisita = eventoRepository.findByTituloContainingIgnoreCase("visita"); //esto e busqueda filtrada de eventos en la base
        List<Evento> conTriana = eventoRepository.findByTituloContainingIgnoreCase("TRIANA");
        List<Evento> sinResultados = eventoRepository.findByTituloContainingIgnoreCase("xyz");

        System.out.println("búsqueda por título");
        System.out.println("Eventos con 'visita' (" + conVisita.size() + "):");
        conVisita.forEach(e -> System.out.println(" - " + e.getTitulo()));
        System.out.println("Eventos con 'TRIANA' (" + conTriana.size() + "):");
        conTriana.forEach(e -> System.out.println(" - " + e.getTitulo()));
        System.out.println("Eventos con 'xyz': " + sinResultados.size());

        assertEquals(5, conVisita.size()); //comporobamos que el valor sea el esperado
        assertEquals(1, conTriana.size());
        assertEquals(0, sinResultados.size());
    }

    @Test
    @DisplayName("La inscripción de un usuario en un evento") //RN-04
    void inscripcionDeUnUsuario() {
        // Buscamos a Carlos y al evento del estadio
        Usuario carlos = usuarioRepository.findByUsuario("cruiz").get();
        Evento estadio = eventoRepository.findByTituloContainingIgnoreCase("estadio").get(0);
        Evento camino = eventoRepository.findByTituloContainingIgnoreCase("Camino").get(0);

        // Carlos está apuntao al estadio, pero no al Camino de Santiago
        Optional<Inscripcion> enEstadio = inscripcionRepository.findByUsuarioIdAndEventoId(carlos.getId(), estadio.getId());
        Optional<Inscripcion> enCamino = inscripcionRepository.findByUsuarioIdAndEventoId(carlos.getId(), camino.getId());

        System.out.println("Inscripciones de " + carlos.getUsuario() + " (id " + carlos.getId() + ")");
        System.out.println("Evento '" + estadio.getTitulo() + "' -> inscrito: " + enEstadio.isPresent());
        System.out.println("Evento '" + camino.getTitulo() + "' -> inscrito: " + enCamino.isPresent());

        assertTrue(enEstadio.isPresent());
        assertFalse(enCamino.isPresent());
    }

    @Test
    @DisplayName("Contar las inscripciones del estadio por estado") //RN-05
    void contarListaDeEspera() {
        Evento estadio = eventoRepository.findByTituloContainingIgnoreCase("estadio").get(0);

        long enEspera = inscripcionRepository.countByEventoIdAndEstado(estadio.getId(), EstadoInscripcion.EN_ESPERA); //cuenta incrip en estado de espera
        long confirmadas = inscripcionRepository.countByEventoIdAndEstado(estadio.getId(), EstadoInscripcion.CONFIRMADA);

        assertEquals(2, enEspera);
        assertEquals(4, confirmadas);
    }

    @Test
    @DisplayName("El primero de la lista de espera del estadio")//RN-05
    void primeroEnEspera() {
        Evento estadio = eventoRepository.findByTituloContainingIgnoreCase("estadio").get(0);

        Optional<Inscripcion> primero = inscripcionRepository
                .findFirstByEventoIdAndEstadoOrderByCreadaEnAsc(estadio.getId(), EstadoInscripcion.EN_ESPERA); //buscamo la first person en la lista de espera
                                                                                                               // de un evento especifiiico

        // El primero en la lista de espera es Sergio (usuario "svega")
        assertTrue(primero.isPresent());
        assertEquals("svega", primero.get().getUsuario().getUsuario());
    }

    @Test
    @DisplayName("Mi agenda de Carlos por tipo de inscripción")//enfdpoint-11
    void miAgenda() {
        Usuario carlos = usuarioRepository.findByUsuario("cruiz").get();

        List<Inscripcion> asistire = inscripcionRepository.findByUsuarioIdAndTipo(carlos.getId(), TipoInscripcion.ASISTIRE);
        List<Inscripcion> meInteresa = inscripcionRepository.findByUsuarioIdAndTipo(carlos.getId(), TipoInscripcion.ME_INTERESA);

        assertEquals(5, asistire.size());
        assertEquals(1, meInteresa.size());
    }
}
