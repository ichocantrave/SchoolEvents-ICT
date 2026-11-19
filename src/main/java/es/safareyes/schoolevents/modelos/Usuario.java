package es.safareyes.schoolevents.modelos;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario")
@Getter @Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id;

    // Solo los estudiantes tienen curso
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_curso")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Curso curso;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "apellidos")
    private String apellidos;

    @Column(name = "email")
    private String email;

    @Column(name = "usuario")
    private String usuario;

    @Column(name = "contrasena")
    @ToString.Exclude
    private String contrasena;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol")
    private RolUsuario rol = RolUsuario.ESTUDIANTE;
}
