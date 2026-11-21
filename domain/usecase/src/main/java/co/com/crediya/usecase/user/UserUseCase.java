package co.com.crediya.usecase.user;

import co.com.crediya.model.auth.UsuarioAutenticado;
import co.com.crediya.model.auth.gateways.PasswordEncoderGateway;
import co.com.crediya.model.exceptions.AccesoDenegadoException;
import co.com.crediya.model.exceptions.CorreoYaRegistradoException;
import co.com.crediya.model.exceptions.DocumentoYaRegistradoException;
import co.com.crediya.model.rol.Rol;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.UserValidator;
import co.com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoderGateway passwordEncoder;

    /**
     * Registra un usuario. Solo lo puede hacer un administrador o un asesor que inició sesión.
     */
    public User registrar(User user, UsuarioAutenticado solicitante) {
        validarPermiso(solicitante);
        UserValidator.validar(user);
        User normalizado = user.normalizado();
        validarCorreoDisponible(normalizado);
        validarDocumentoDisponible(normalizado);
        return guardarConClaveEncriptada(normalizado);
    }

    /**
     * Crea el primer administrador al arrancar la aplicación, si todavía no existe.
     * Sin él nadie podría iniciar sesión para registrar usuarios.
     */
    public boolean crearAdministradorInicial(User admin) {
        User conRol = admin.toBuilder().idRol(Rol.ADMIN.getId()).build();
        UserValidator.validar(conRol);
        User normalizado = conRol.normalizado();
        if (userRepository.existsByCorreoElectronico(normalizado.getCorreoElectronico())) {
            return false;
        }
        guardarConClaveEncriptada(normalizado);
        return true;
    }

    private static void validarPermiso(UsuarioAutenticado solicitante) {
        if (solicitante == null || !solicitante.tieneAlgunRol(Rol.ADMIN, Rol.ASESOR)) {
            throw new AccesoDenegadoException("Solo un administrador o un asesor puede registrar usuarios");
        }
    }

    private User guardarConClaveEncriptada(User user) {
        return userRepository.save(user.conClaveEncriptada(passwordEncoder.encriptar(user.getClave())));
    }

    private void validarCorreoDisponible(User user) {
        if (userRepository.existsByCorreoElectronico(user.getCorreoElectronico())) {
            throw new CorreoYaRegistradoException();
        }
    }

    private void validarDocumentoDisponible(User user) {
        if (user.getDocumentoIdentidad() != null
                && userRepository.existsByDocumentoIdentidad(user.getDocumentoIdentidad())) {
            throw new DocumentoYaRegistradoException();
        }
    }
}
