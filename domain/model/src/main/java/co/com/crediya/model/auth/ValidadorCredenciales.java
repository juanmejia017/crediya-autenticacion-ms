package co.com.crediya.model.auth;

import co.com.crediya.model.exceptions.CredencialesInvalidasException;
import co.com.crediya.model.exceptions.DatosInvalidosException;
import co.com.crediya.model.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * Validaciones del inicio de sesión: que vengan correo y clave, que el usuario exista y que la clave coincida.
 * Si el usuario no existe o la clave no coincide se lanza el mismo error, para no dar pistas.
 */
public final class ValidadorCredenciales {

    private ValidadorCredenciales() {
    }

    public static void validarDatos(String correoElectronico, String clave) {
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

    public static User validarUsuarioRegistrado(Optional<User> usuario) {
        return usuario.orElseThrow(CredencialesInvalidasException::new);
    }

    /**
     * La clave escrita debe coincidir con la encriptada. Recibe la comparación del encriptador como función.
     */
    public static void validarClave(String clave, User usuario, BiPredicate<String, String> coincide) {
        if (usuario.getClave() == null || !coincide.test(clave, usuario.getClave())) {
            throw new CredencialesInvalidasException();
        }
    }
}
