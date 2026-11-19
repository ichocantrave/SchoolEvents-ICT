package es.safareyes.schoolevents.modelos;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inscripcion")
@Getter @Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Inscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inscripcion")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evento")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Evento evento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo")
    private TipoInscripcion tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoInscripcion estado = EstadoInscripcion.CONFIRMADA;

    @Column(name = "asistio")
    private Boolean asistio = false;

    @Column(name = "creada_en", nullable = false, insertable = false, updatable = false)
    private LocalDateTime creadaEn;
}
