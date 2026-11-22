package es.safareyes.schoolevents.modelos;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cambio_evento")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CambioEvento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cambio")
    private Integer id;

    // Event its modify
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evento")
    private Evento evento;

    // User ha realizao el change
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autor")
    private Usuario autor;

    // Campo modificao
    @Column(name = "campo")
    private String campo;

    // Valor anterior
    @Column(name = "valor_anterior")
    private String valorAnterior;

    // Valor new
    @Column(name = "valor_nuevo")
    private String valorNuevo;

    // Fecha realizara el cmabio
    @Column(name = "fecha_cambio")
    private LocalDateTime fechaCambio;
}
