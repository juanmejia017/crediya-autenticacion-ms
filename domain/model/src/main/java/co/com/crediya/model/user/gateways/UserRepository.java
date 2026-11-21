package co.com.crediya.model.user.gateways;

import co.com.crediya.model.user.User;

import java.util.Optional;

public interface UserRepository {
    User guardar(User user);
    boolean existePorCorreoElectronico(String correoElectronico);
    boolean existePorDocumentoIdentidad(String documentoIdentidad);
    Optional<User> buscarPorCorreoElectronico(String correoElectronico);
}
