package co.com.crediya.usecase.autenticacion;

import co.com.crediya.model.auth.Sesion;
import co.com.crediya.model.auth.UsuarioAutenticado;
import co.com.crediya.model.auth.gateways.PasswordEncoderGateway;
import co.com.crediya.model.auth.gateways.TokenGateway;
import co.com.crediya.model.exceptions.CredencialesInvalidasException;
import co.com.crediya.model.exceptions.DatosInvalidosException;
import co.com.crediya.model.exceptions.NoAutenticadoException;
import co.com.crediya.model.exceptions.TechnicalException;
import co.com.crediya.model.rol.Rol;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public class AutenticacionUseCase {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final UserRepository userRepository;
    private final PasswordEncoderGateway passwordEncoder;
    private final TokenGateway tokenGateway;

    /**
     * Inicia sesión con correo y clave. No hay límite de intentos.
     */
    public Sesion iniciarSesion(String correoElectronico, String clave) {
        validarCredenciales(correoElectronico, clave);
        String correo = correoElectronico.trim().toLowerCase(Locale.ROOT);

        User usuario = userRepository.buscarPorCorreoElectronico(correo)
                .orElseThrow(CredencialesInvalidasException::new);
        if (usuario.getClave() == null || !passwordEncoder.coincide(clave, usuario.getClave())) {
            throw new CredencialesInvalidasException();
        }

        Rol rol = Rol.porId(usuario.getIdRol())
                .orElseThrow(() -> new TechnicalException("ROL_NO_CONFIGURADO",
                        "El usuario " + usuario.getIdUsuario() + " no tiene un rol válido"));
        return tokenGateway.generar(new UsuarioAutenticado(
                usuario.getIdUsuario(), usuario.getCorreoElectronico(), usuario.getDocumentoIdentidad(), rol));
    }

    /**
     * Recibe el header Authorization ("Bearer <token>") y devuelve quién hizo la petición.
     */
    public UsuarioAutenticado validarToken(String authorization) {
        if (authorization == null || !authorization.startsWith(PREFIJO_BEARER)) {
            throw new NoAutenticadoException("Debes iniciar sesión y enviar el token en el header Authorization: Bearer <token>");
        }
        String token = authorization.substring(PREFIJO_BEARER.length()).trim();
        return tokenGateway.validar(token)
                .orElseThrow(() -> new NoAutenticadoException("El token es inválido o ya expiró. Inicia sesión de nuevo"));
    }

    private static void validarCredenciales(String correoElectronico, String clave) {
        List<String> errores = new ArrayList<>();
        if (correoElectronico == null || correoElectronico.isBlank()) {
            errores.add("El campo correoElectronico es obligatorio");
        }
        if (clave == null || clave.isBlank()) {
            errores.add("El campo clave es obligatorio");
        }
        if (!errores.isEmpty()) {
            throw new DatosInvalidosException(errores);
        }
    }
}
