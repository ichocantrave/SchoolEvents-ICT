package es.safareyes.schoolevents.modelos;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "publicacion")
@Getter @Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Publicacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_publicacion")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evento")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    @Column(name = "texto")
    private String texto;

    @Column(name = "fecha", nullable = false, insertable = false, updatable = false)
    private LocalDateTime fecha;

    // Lado inverso: así el endpoint 15 puede cargar publicaciones con sus fotos (@EntityGraph)
    @OneToMany(mappedBy = "publicacion", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<FotoPublicacion> fotos;
}
