package co.com.crediya.model.user.gateways;

import co.com.crediya.model.user.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    boolean existsByCorreoElectronico(String correoElectronico);
    boolean existsByDocumentoIdentidad(String documentoIdentidad);
    Optional<User> findByCorreoElectronico(String correoElectronico);
}
