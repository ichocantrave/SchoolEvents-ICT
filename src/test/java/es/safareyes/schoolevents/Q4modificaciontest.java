package es.safareyes.schoolevents;

import es.safareyes.schoolevents.repositorios.EventoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Q4 consulta de modificacion con modifying, esto es lo que he eliminado en e repositorio de evento
// La usará la tarea programada que marca como finalizao los eventos que ya terminaron
// @DataJpaTest deshace los cambios al acabar el test, así que no toca la base de datos
@DisplayName("Modificación") //Q4
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class Q4ModificacionTest {

    @Autowired
    private EventoRepository eventoRepository;

    @Test
    @DisplayName("Los eventos publicaos que ya terminaron pasan a finalizao")
    void finalizarEventosTerminados() {
        // Simulamos que hoy es el 1 de diciembre de 2026 porque ojalá sea diciembre
        int modificados = eventoRepository.finalizarTerminados(LocalDateTime.of(2026, 12, 1, 0, 0));

        System.out.println("Eventos pasados a FINALIZADO: " + modificados); //Q4
        // CICA, estadio y Casa de la Ciencia ya habían terminado perooo IMSE-CNM es el 15 de diciembre
        assertEquals(3, modificados);
    }
}