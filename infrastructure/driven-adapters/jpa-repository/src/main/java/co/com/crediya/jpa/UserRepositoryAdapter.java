package co.com.crediya.jpa;

import co.com.crediya.jpa.error.TraductorErroresIntegridad;
import co.com.crediya.jpa.mapper.UserEntityMapper;
import co.com.crediya.jpa.repository.UserJpaRepository;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Persistencia de usuarios en la tabla usuarios.
 * La conversión la hace UserEntityMapper y la traducción de errores TraductorErroresIntegridad.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository repository;

    @Override
    @Transactional
    public User guardar(User user) {
        try {
            User guardado = UserEntityMapper.aDominio(repository.save(UserEntityMapper.aEntidad(user)));
            log.info("Usuario persistido con id {}", guardado.getIdUsuario());
            return guardado;
        } catch (DataIntegrityViolationException e) {
            throw TraductorErroresIntegridad.traducir(e);
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
        return repository.findByCorreoElectronico(correoElectronico).map(UserEntityMapper::aDominio);
    }
}
