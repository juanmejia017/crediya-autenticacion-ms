package co.com.crediya.usecase.user;

import co.com.crediya.model.auth.UsuarioAutenticado;
import co.com.crediya.model.auth.gateways.PasswordEncoderGateway;
import co.com.crediya.model.rol.Rol;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.ValidadorUsuario;
import co.com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.Locale;

/**
 * Registro de usuarios. Solo orquesta: las validaciones están en ValidadorUsuario.
 */
@RequiredArgsConstructor
public class UserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoderGateway passwordEncoder;

    public User registrarUsuario(User user, UsuarioAutenticado solicitante) {
        ValidadorUsuario.validarPermisoDeRegistro(solicitante);
        ValidadorUsuario.validarDatos(user);
        User normalizado = user.normalizado();
        ValidadorUsuario.validarCorreoDisponible(normalizado, userRepository::existePorCorreoElectronico);
        ValidadorUsuario.validarDocumentoDisponible(normalizado, userRepository::existePorDocumentoIdentidad);
        return guardarConClaveEncriptada(normalizado);
    }

    public boolean existeUsuarioConCorreo(String correoElectronico) {
        return userRepository.existePorCorreoElectronico(correoElectronico.trim().toLowerCase(Locale.ROOT));
    }

    /**
     * Crea el primer administrador. Quien lo llama (al arrancar) revisa antes que no exista.
     */
    public User crearAdministradorInicial(User admin) {
        User administrador = admin.toBuilder().idRol(Rol.ADMIN.getId()).build();
        ValidadorUsuario.validarDatos(administrador);
        return guardarConClaveEncriptada(administrador.normalizado());
    }

    private User guardarConClaveEncriptada(User user) {
        return userRepository.guardar(user.conClaveEncriptada(passwordEncoder.encriptar(user.getClave())));
    }
}
