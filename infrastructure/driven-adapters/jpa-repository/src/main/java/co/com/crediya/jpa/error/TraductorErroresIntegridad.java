package co.com.crediya.jpa.error;

import co.com.crediya.jpa.entity.UserEntity;
import co.com.crediya.model.exceptions.CorreoYaRegistradoException;
import co.com.crediya.model.exceptions.DocumentoYaRegistradoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Locale;

/**
 * Convierte una violación de restricción única de la base de datos en la excepción del dominio.
 * Pasa cuando dos registros con el mismo correo o documento llegan al mismo tiempo.
 */
@Slf4j
public final class TraductorErroresIntegridad {

    private TraductorErroresIntegridad() {
    }

    public static RuntimeException traducir(DataIntegrityViolationException e) {
        String causa = String.valueOf(e.getMostSpecificCause().getMessage()).toLowerCase(Locale.ROOT);
        if (causa.contains(UserEntity.UK_CORREO)) {
            log.warn("Restricción única violada: {}", UserEntity.UK_CORREO);
            return new CorreoYaRegistradoException();
        }
        if (causa.contains(UserEntity.UK_DOCUMENTO)) {
            log.warn("Restricción única violada: {}", UserEntity.UK_DOCUMENTO);
            return new DocumentoYaRegistradoException();
        }
        log.error("Violación de integridad no controlada al guardar el usuario", e);
        return e;
    }
}
