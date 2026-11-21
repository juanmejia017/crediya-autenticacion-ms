package co.com.crediya.model.exceptions;

/**
 * Correo o clave incorrectos. El mensaje no dice cuál de los dos falló para no dar pistas.
 */
public class CredencialesInvalidasException extends BusinessException {
    public CredencialesInvalidasException() {
        super("CREDENCIALES_INVALIDAS", "El correo o la clave son incorrectos");
    }
}
