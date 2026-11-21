package co.com.crediya.jpa;

import co.com.crediya.jpa.entity.UserEntity;
import co.com.crediya.jpa.mapper.UserEntityMapper;
import co.com.crediya.jpa.repository.UserJpaRepository;
import co.com.crediya.model.exceptions.CorreoYaRegistradoException;
import co.com.crediya.model.exceptions.DocumentoYaRegistradoException;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

/**
 * Persistencia de usuarios: guarda y consulta en la tabla usuarios.
 * La conversión entre User y UserEntity la hace UserEntityMapper.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository repository;

    @Override
    @Transactional
    public User guardar(User user) {
        log.info("Persistiendo nuevo usuario");
        try {
            User guardado = UserEntityMapper.aDominio(repository.save(UserEntityMapper.aEntidad(user)));
            log.info("Usuario persistido con id {}", guardado.getIdUsuario());
            return guardado;
        } catch (DataIntegrityViolationException e) {
            throw traducirViolacionDeIntegridad(e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCorreoElectronico(String correoElectronico) {
        return repository.existsByCorreoElectronico(correoElectronico);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorDocumentoIdentidad(String documentoIdentidad) {
        return repository.existsByDocumentoIdentidad(documentoIdentidad);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> buscarPorCorreoElectronico(String correoElectronico) {
        Optional<User> usuario = repository.findByCorreoElectronico(correoElectronico).map(UserEntityMapper::aDominio);
        if (usuario.isEmpty()) {
            log.warn("No existe un usuario con el correo indicado");
        }
        return usuario;
    }

    /**
     * Si dos registros llegan al mismo tiempo, la restricción única de la base de datos es la que frena el segundo.
     */
    private RuntimeException traducirViolacionDeIntegridad(DataIntegrityViolationException e) {
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
