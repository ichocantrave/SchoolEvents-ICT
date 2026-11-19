package es.safareyes.schoolevents;

import es.safareyes.schoolevents.dto.EventoCalendarioView;
import es.safareyes.schoolevents.repositorios.EventoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Q3 con sql normal y nativequery como true
// Calendario  del mes
@DisplayName("Consulta nativa") //Q3
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class Q3NativaTest {

    @Autowired
    private EventoRepository eventoRepository;

    @Test
    @DisplayName("Calendario de noviembre de 2026") //endpoint2
    void calendarioDeNoviembre() {
        // Mes 11 del año 2026
        List<EventoCalendarioView> calendario = eventoRepository.calendarioDelMes(11, 2026);

        System.out.println("Eventos en el calendario de noviembre de 2026: " + calendario.size()); //Q3
        // Hay 3 eventos que estan ordenados por día
        assertEquals(3, calendario.size());
        assertEquals("CICA", calendario.get(0).getTitulo());
        assertEquals("2026-11-19", calendario.get(0).getDia());
        assertEquals("2026-11-23", calendario.get(1).getDia());
        assertEquals("2026-11-30", calendario.get(2).getDia());
        // Cada evento lleva el icono y el color de su categoría (CICA es Académico)
        assertEquals("icons/academico.png", calendario.get(0).getIcono());
        assertEquals("#2F6FED", calendario.get(0).getColor());
    }

    @Test
    @DisplayName("Een diciembre no sale el evento CANCELADO (Viaje París)") //endpoint 2
    void calendarioSinCancelados() {
        List<EventoCalendarioView> calendario = eventoRepository.calendarioDelMes(12, 2026);

        System.out.println("Eventos en el calendario de diciembre de 2026: " + calendario.size()); //Q3
        // Solo sale IMSE-CNM
        assertEquals(1, calendario.size());
        assertEquals("IMSE-CNM", calendario.get(0).getTitulo());
    }
}
