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
import static org.junit.jupiter.api.Assertions.assertNotNull;

// PROYECCIONES (devolver solo algunos datos, los que tiene la interfaz en EventoCalendarioView)
// Er calendario lleva título, día, horas, icono y color
@DisplayName("Proyecciones") //Q5
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class Q5ProyeccionTest {

    @Autowired
    private EventoRepository eventoRepository;

    @Test
    @DisplayName("Todos los datos del evento CICA") //endpoint 2
    void datosDeUnEvento() {
        List<EventoCalendarioView> calendario = eventoRepository.calendarioDelMes(11, 2026);
        EventoCalendarioView cica = calendario.get(0);

        System.out.println("--- DATOS DE CICA ---");
        System.out.println("Título: " + cica.getTitulo());
        System.out.println("Día: " + cica.getDia());
        System.out.println("Color: " + cica.getColor());
        System.out.println("---------------------");

        assertNotNull(cica.getId()); //pa asegurarnos que el ID de este evento no sea nulo
        assertEquals("CICA", cica.getTitulo());
        assertEquals("2026-11-19", cica.getDia());
        assertEquals("08:00", cica.getHoraInicio());
        assertEquals("14:30", cica.getHoraFin());
        assertEquals("icons/academico.png", cica.getIcono());
        assertEquals("#2F6FED", cica.getColor());
    }

    @Test
    @DisplayName("Cada evento lleva el icono y el color de su categoría")//endpoint 2
    void iconoYColorPorCategoria() {
        // Abril de 2027: Feria de Ciencias (Académico), Visitas hermandades (Religioso) y El Quijote (Cultural)
        List<EventoCalendarioView> abril = eventoRepository.calendarioDelMes(4, 2027);

        assertEquals(3, abril.size());
        assertEquals("#2F6FED", abril.get(0).getColor());   // Académico: azul
        assertEquals("#6C3FA0", abril.get(1).getColor());   // Religioso: morado
        assertEquals("#F1C40F", abril.get(2).getColor());   // Cultural: amarillo

        // pd: se me da bien la eleccion de colores
    }
}
