package co.com.crediya.usecase.autenticacion;

import co.com.crediya.model.auth.CredencialesValidator;
import co.com.crediya.model.auth.Sesion;
import co.com.crediya.model.auth.UsuarioAutenticado;
import co.com.crediya.model.auth.gateways.PasswordEncoderGateway;
import co.com.crediya.model.auth.gateways.TokenGateway;
import co.com.crediya.model.exceptions.CredencialesInvalidasException;
import co.com.crediya.model.exceptions.NoAutenticadoException;
import co.com.crediya.model.exceptions.TechnicalException;
import co.com.crediya.model.rol.Rol;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.Locale;

/**
 * Autenticación: iniciar sesión con correo y clave, y reconocer al usuario de un token.
 */
@RequiredArgsConstructor
public class AutenticacionUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoderGateway passwordEncoder;
    private final TokenGateway tokenGateway;

    /**
     * Inicia sesión con correo y clave. No hay límite de intentos.
     */
    public Sesion iniciarSesion(String correoElectronico, String clave) {
        CredencialesValidator.validar(correoElectronico, clave);
        User usuario = buscarUsuarioConClaveCorrecta(correoElectronico.trim().toLowerCase(Locale.ROOT), clave);
        return tokenGateway.generar(aUsuarioAutenticado(usuario));
    }

    /**
     * Devuelve quién es el dueño del token, o lanza NoAutenticadoException si el token no sirve.
     */
    public UsuarioAutenticado validarToken(String token) {
        if (token == null || token.isBlank()) {
            throw new NoAutenticadoException("Debes iniciar sesión y enviar el token en el header Authorization: Bearer <token>");
        }
        return tokenGateway.validar(token)
                .orElseThrow(() -> new NoAutenticadoException("El token es inválido o ya expiró. Inicia sesión de nuevo"));
    }

    private User buscarUsuarioConClaveCorrecta(String correo, String clave) {
        User usuario = userRepository.buscarPorCorreoElectronico(correo)
                .orElseThrow(CredencialesInvalidasException::new);
        if (usuario.getClave() == null || !passwordEncoder.coincide(clave, usuario.getClave())) {
            throw new CredencialesInvalidasException();
        }
        return usuario;
    }

    private static UsuarioAutenticado aUsuarioAutenticado(User usuario) {
        Rol rol = Rol.porId(usuario.getIdRol())
                .orElseThrow(() -> new TechnicalException("ROL_NO_CONFIGURADO",
                        "El usuario " + usuario.getIdUsuario() + " no tiene un rol válido"));
        return new UsuarioAutenticado(
                usuario.getIdUsuario(), usuario.getCorreoElectronico(), usuario.getDocumentoIdentidad(), rol);
    }
}
