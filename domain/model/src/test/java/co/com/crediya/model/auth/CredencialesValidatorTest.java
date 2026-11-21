package co.com.crediya.model.auth;

import co.com.crediya.model.exceptions.DatosInvalidosException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CredencialesValidatorTest {

    @Test
    void aceptaCorreoYClave() {
        assertDoesNotThrow(() -> CredencialesValidator.validar("ana@correo.com", "Clave123*"));
    }

    @Test
    void acumulaLosDosErroresSiFaltanAmbos() {
        DatosInvalidosException ex = assertThrows(DatosInvalidosException.class,
                () -> CredencialesValidator.validar(" ", null));

        assertEquals(2, ex.getErrores().size());
    }
}
