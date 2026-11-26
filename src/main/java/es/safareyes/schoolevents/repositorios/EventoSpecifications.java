package es.safareyes.schoolevents.repositorios;

import es.safareyes.schoolevents.modelos.Evento;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import jakarta.persistence.criteria.JoinType;

// Q7: filtros que se pueden combinar con .and()
public class EventoSpecifications {

    public static Specification<Evento> conCategoria(Integer idCategoria) {
        return (root, query, cb) -> cb.equal(root.get("categoria").get("id"), idCategoria);
    }
    //un metodo que fltra pa buscar to los eventos que pertenezcan a una categoria especifica.

    public static Specification<Evento> tituloContiene(String texto) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("titulo")), "%" + texto.toLowerCase() + "%");
    }

    // Filtramos por eventos por un interval de fechas de ini
    public static Specification<Evento> entreFechas(
            LocalDateTime desde,
            LocalDateTime hasta) {

        return (root, query, cb) -> cb.between(root.get("fechaInicio"), desde, hasta);
    }

    // Busca eventos de un curso o abiertos a to el centro
    public static Specification<Evento> conCurso(Integer idCurso) {
        return (root, query, cb) -> {
            // Create consult in otra consult
            var subconsulta = query.subquery(Integer.class);
            // Relation consult w main event
            var eventoCurso = subconsulta.correlate(root);
            var curso = eventoCurso.join("cursos");
            // a ve si tienen el id
            subconsulta.select(curso.get("id")).where(cb.equal(curso.get("id"), idCurso));
            // Show event open o  curso solicitado
            return cb.or(
                    cb.isEmpty(root.get("cursos")),
                    cb.exists(subconsulta)
            );
        };
    }


}

