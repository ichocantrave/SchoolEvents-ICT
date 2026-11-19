package es.safareyes.schoolevents.modelos;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter @Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioNotificacionID implements Serializable {
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "id_notificacion")
    private Integer idNotificacion;
}
//Esto es porque la columna leida esta, por lo tanto no srve el to May, ya que este necesita su propia identidad
