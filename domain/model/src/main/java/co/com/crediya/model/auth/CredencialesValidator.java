package co.com.crediya.model.auth;

import co.com.crediya.model.exceptions.DatosInvalidosException;

import java.util.ArrayList;
import java.util.List;

/**
 * Reglas de formato de las credenciales del inicio de sesión: correo y clave obligatorios.
 */
public final class CredencialesValidator {

    private CredencialesValidator() {
    }

    public static void validar(String correoElectronico, String clave) {
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
