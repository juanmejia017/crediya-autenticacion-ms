package co.com.crediya.model.auth;

/**
 * Resultado de un inicio de sesión: el token y cuánto dura.
 */
public record Sesion(String token, String tipo, long expiraEnSegundos, UsuarioAutenticado usuario) {

    public static final String TIPO_BEARER = "Bearer";
}
