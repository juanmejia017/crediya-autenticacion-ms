package co.com.crediya.model.auth;

import co.com.crediya.model.exceptions.CredencialesInvalidasException;
import co.com.crediya.model.exceptions.DatosInvalidosException;
import co.com.crediya.model.user.User;

import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidadorCredencialesTest {

    @Test
    void aceptaCorreoYClave() {
        assertDoesNotThrow(() -> ValidadorCredenciales.validarDatos("ana@correo.com", "Clave123*"));
    }

    @Test
    void acumulaLosDosErroresSiFaltanAmbos() {
        DatosInvalidosException ex = assertThrows(DatosInvalidosException.class,
                () -> ValidadorCredenciales.validarDatos(" ", null));

        assertEquals(2, ex.getErrores().size());
    }

    @Test
    void elUsuarioDebeExistir() {
        User usuario = User.builder().correoElectronico("ana@correo.com").build();

        assertEquals(usuario, ValidadorCredenciales.validarUsuarioRegistrado(Optional.of(usuario)));
        assertThrows(CredencialesInvalidasException.class, () -> ValidadorCredenciales.validarUsuarioRegistrado(Optional.empty()));
    }

    @Test
    void laClaveDebeCoincidir() {
        User usuario = User.builder().clave("hash").build();

        assertDoesNotThrow(() -> ValidadorCredenciales.validarClave("Clave123*", usuario, (clave, hash) -> true));
        assertThrows(CredencialesInvalidasException.class,
                () -> ValidadorCredenciales.validarClave("otra", usuario, (clave, hash) -> false));
        assertThrows(CredencialesInvalidasException.class,
                () -> ValidadorCredenciales.validarClave("Clave123*", User.builder().build(), (clave, hash) -> true));
    }
}
