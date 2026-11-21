package co.com.crediya.usecase.autenticacion;

import co.com.crediya.model.auth.Sesion;
import co.com.crediya.model.auth.UsuarioAutenticado;
import co.com.crediya.model.auth.ValidadorCredenciales;
import co.com.crediya.model.auth.ValidadorToken;
import co.com.crediya.model.auth.gateways.PasswordEncoderGateway;
import co.com.crediya.model.auth.gateways.TokenGateway;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.Locale;

/**
 * Autenticación: iniciar sesión y reconocer al usuario de un token.
 * Solo orquesta: las validaciones están en ValidadorCredenciales y ValidadorToken.
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
        ValidadorCredenciales.validarDatos(correoElectronico, clave);
        String correo = correoElectronico.trim().toLowerCase(Locale.ROOT);
        User usuario = ValidadorCredenciales.validarUsuarioRegistrado(userRepository.buscarPorCorreoElectronico(correo));
        ValidadorCredenciales.validarClave(clave, usuario, passwordEncoder::coincide);
        return tokenGateway.generar(UsuarioAutenticado.desdeUsuario(usuario));
    }

    public UsuarioAutenticado validarToken(String token) {
        ValidadorToken.validarPresente(token);
        return ValidadorToken.validarVigente(tokenGateway.validar(token));
    }
}
