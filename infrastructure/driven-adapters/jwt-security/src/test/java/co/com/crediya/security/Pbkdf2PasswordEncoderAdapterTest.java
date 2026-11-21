package co.com.crediya.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Pbkdf2PasswordEncoderAdapterTest {

    private final Pbkdf2PasswordEncoderAdapter encoder = new Pbkdf2PasswordEncoderAdapter();

    @Test
    void laClaveEncriptadaNoContieneLaOriginalYCoincide() {
        String hash = encoder.encriptar("Clave123*");

        assertTrue(hash.startsWith("pbkdf2$"));
        assertFalse(hash.contains("Clave123*"));
        assertTrue(encoder.coincide("Clave123*", hash));
    }

    @Test
    void rechazaClaveIncorrecta() {
        assertFalse(encoder.coincide("otra", encoder.encriptar("Clave123*")));
    }

    @Test
    void laMismaClaveProduceHashesDistintosPorLaSal() {
        assertNotEquals(encoder.encriptar("Clave123*"), encoder.encriptar("Clave123*"));
    }

    @Test
    void rechazaValoresInvalidos() {
        assertFalse(encoder.coincide("Clave123*", null));
        assertFalse(encoder.coincide(null, "pbkdf2$1$a$b"));
        assertFalse(encoder.coincide("Clave123*", "texto-plano"));
        assertFalse(encoder.coincide("Clave123*", "pbkdf2$x$y$z"));
    }
}
