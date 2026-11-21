package co.com.crediya.security;

import co.com.crediya.model.auth.Sesion;
import co.com.crediya.model.auth.UsuarioAutenticado;
import co.com.crediya.model.rol.Rol;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenAdapterTest {

    private static final String SECRETO = "clave-secreta-de-pruebas-con-mas-de-32-caracteres";
    private static final Instant AHORA = Instant.parse("2026-11-21T12:00:00Z");
    private static final UsuarioAutenticado CLIENTE =
            new UsuarioAutenticado(7L, "ana@correo.com", "1020304050", Rol.CLIENTE);

    private JwtTokenAdapter adapterEn(Instant instante) {
        return new JwtTokenAdapter(SECRETO, 60, Clock.fixed(instante, ZoneOffset.UTC));
    }

    @Test
    void generaUnTokenQueLuegoSeValidaConLosMismosDatos() {
        Sesion sesion = adapterEn(AHORA).generar(CLIENTE);

        assertEquals("Bearer", sesion.tipo());
        assertEquals(3600, sesion.expiraEnSegundos());
        assertEquals(3, sesion.token().split("\\.").length);
        assertEquals(Optional.of(CLIENTE), adapterEn(AHORA.plusSeconds(60)).validar(sesion.token()));
    }

    @Test
    void funcionaSinDocumento() {
        UsuarioAutenticado admin = new UsuarioAutenticado(1L, "admin@crediya.com", null, Rol.ADMIN);
        String token = adapterEn(AHORA).generar(admin).token();

        assertEquals(Optional.of(admin), adapterEn(AHORA).validar(token));
    }

    @Test
    void rechazaTokenExpirado() {
        String token = adapterEn(AHORA).generar(CLIENTE).token();

        assertTrue(adapterEn(AHORA.plus(Duration.ofMinutes(61))).validar(token).isEmpty());
    }

    @Test
    void rechazaTokenFirmadoConOtroSecreto() {
        JwtTokenAdapter otro = new JwtTokenAdapter("otro-secreto-distinto-tambien-de-mas-de-32", 60,
                Clock.fixed(AHORA, ZoneOffset.UTC));
        String token = otro.generar(CLIENTE).token();

        assertTrue(adapterEn(AHORA).validar(token).isEmpty());
    }

    @Test
    void rechazaTokenModificadoParaCambiarElRol() {
        String[] partes = adapterEn(AHORA).generar(CLIENTE).token().split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8)
                .replace("CLIENTE", "ADMIN");
        String alterado = partes[0] + "." + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "." + partes[2];

        assertTrue(adapterEn(AHORA).validar(alterado).isEmpty());
    }

    @Test
    void rechazaBasura() {
        JwtTokenAdapter adapter = adapterEn(AHORA);

        assertTrue(adapter.validar("no-es-un-token").isEmpty());
        assertTrue(adapter.validar("a.b.c").isEmpty());
        assertTrue(adapter.validar("").isEmpty());
    }

    @Test
    void exigeUnSecretoDeAlMenos32Caracteres() {
        assertThrows(IllegalArgumentException.class, () -> new JwtTokenAdapter("corto", 60));
        assertThrows(IllegalArgumentException.class, () -> new JwtTokenAdapter(null, 60));
        assertThrows(IllegalArgumentException.class, () -> new JwtTokenAdapter(SECRETO, 0));
    }
}
