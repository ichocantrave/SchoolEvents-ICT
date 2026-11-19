package es.safareyes.schoolevents;

import es.safareyes.schoolevents.modelos.Evento;
import es.safareyes.schoolevents.modelos.Publicacion;
import es.safareyes.schoolevents.repositorios.EventoRepository;
import es.safareyes.schoolevents.repositorios.PublicacionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Q8 era el ENTITYGRAPH ( sirve pa traer el evento junto con su lugar, categoría y cursos en una sola consulta)
// Endpoints de GET que habia que poner el detalle del evento y algunas publicaciones con fotitos
@DisplayName("EntityGraph")//Q8
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class Q8EntityGraphTest {

    @Autowired
    private EventoRepository eventoRepository;
    @Autowired
    private PublicacionRepository publicacionRepository;

    @Test
    @DisplayName("Detalle de IMSE-CNM con lugar, categoría y cursos")//endpoint 3
    void detalleDelEvento() {
        Evento imse = eventoRepository.findByTituloContainingIgnoreCase("IMSE").get(0); //recupera eventos especificos

        Evento detalle = eventoRepository.findConDetalleById(imse.getId()).get();

        System.out.println("En el endpoint 3 " + detalle.getTitulo() + ": lugar " + detalle.getLugar().getNombre()
                + ", categoría " + detalle.getCategoria().getNombre() + ", cursos " + detalle.getCursos().size());
        assertEquals("Cartuja", detalle.getLugar().getNombre());
        assertEquals("Académico", detalle.getCategoria().getNombre());
        assertEquals(2, detalle.getCursos().size());   // 2º DAM y 2º Bachillerato
    }

    @Test
    @DisplayName("La Feria de Ciencias no tiene cursos (es para todo el centro)")//enpoint 3
    void eventoSinCursos() {
        Evento feria = eventoRepository.findByTituloContainingIgnoreCase("Feria de Ciencias").get(0);

        Evento detalle = eventoRepository.findConDetalleById(feria.getId()).get();

        System.out.println("Endpoint 3 - " + detalle.getTitulo() + " tiene " + detalle.getCursos().size() + " cursos");
        assertEquals(0, detalle.getCursos().size());
    }

    @Test
    @DisplayName(" En el endpoint 15: Escuela ETSI tiene 2 publicaciones y 3 fotos")
    void publicacionesConFotos() {
        Evento etsi = eventoRepository.findByTituloContainingIgnoreCase("Escuela ETSI").get(0);

        List<Publicacion> publicaciones = publicacionRepository.findByEventoId(etsi.getId());

        // Sumamos las fotos de las dos publicaciones
        int fotos = publicaciones.get(0).getFotos().size() + publicaciones.get(1).getFotos().size();

        System.out.println("En el Endpoint 15 - " + etsi.getTitulo() + ": " + publicaciones.size() + " publicaciones y " + fotos + " fotos");
        assertEquals(2, publicaciones.size());
        assertEquals(3, fotos);
    }
}
