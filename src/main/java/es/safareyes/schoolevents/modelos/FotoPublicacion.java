package es.safareyes.schoolevents.modelos;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "foto_publicacion")
@Getter @Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FotoPublicacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_foto")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_publicacion")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Publicacion publicacion;

    @Column(name = "ruta")
    private String ruta;

    @Column(name = "descripcion")
    private String descripcion;
}
