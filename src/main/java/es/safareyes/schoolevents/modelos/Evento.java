package es.safareyes.schoolevents.modelos;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "evento")
@Getter @Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lugar")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Lugar lugar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_organizador")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario organizador;

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "aforo")
    private Integer aforo;

    @Column(name = "fecha_limite")
    private LocalDateTime fechaLimite;

    @Column(name = "cartel")
    private String cartel;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoEvento estado = EstadoEvento.BORRADOR;

    // Si la lista de cursos está vacía, el evento es pa to el centro
    //metemo el código de curso_evento, evento_notificacion y de evento_requisito
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "curso_evento",
            joinColumns = @JoinColumn(name = "id_evento"),
            inverseJoinColumns = @JoinColumn(name = "id_curso")
    ) //un evento puede ser dirigido por varios cursos, un curso puede tener varios eventos, tabla curso_evento save que curso pertene a each event.
      // si un event don´t have cursos asignaos, is open a to el centro.
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Curso> cursos;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "evento_requisito",
            joinColumns = @JoinColumn(name = "id_evento"),
            inverseJoinColumns = @JoinColumn(name = "id_requisito")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Requisito> requisitos;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "evento_notificacion",
            joinColumns = @JoinColumn(name = "id_evento"),
            inverseJoinColumns = @JoinColumn(name = "id_notificacion")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Notificacion> notificaciones;
}
