package es.safareyes.schoolevents.modelos;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario_notificacion")
@Getter @Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UsuarioNotificacion {
    @EmbeddedId
    private UsuarioNotificacionID id = new UsuarioNotificacionID();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idUsuario")
    @JoinColumn(name = "id_usuario")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idNotificacion")
    @JoinColumn(name = "id_notificacion")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Notificacion notificacion;

    @Column(name = "leida")
    private Boolean leida = false;
}
