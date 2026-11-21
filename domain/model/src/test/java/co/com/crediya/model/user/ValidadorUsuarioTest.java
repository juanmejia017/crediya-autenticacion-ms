package co.com.crediya.model.user;

import co.com.crediya.model.auth.UsuarioAutenticado;
import co.com.crediya.model.exceptions.AccesoDenegadoException;
import co.com.crediya.model.exceptions.CorreoYaRegistradoException;
import co.com.crediya.model.exceptions.DocumentoYaRegistradoException;
import co.com.crediya.model.rol.Rol;

import co.com.crediya.model.exceptions.DatosInvalidosException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidadorUsuarioTest {

    private User.UserBuilder usuarioValido() {
        return User.builder()
                .nombres("Juan")
                .apellidos("Mejia")
                .correoElectronico("juan@correo.com")
                .documentoIdentidad("1020304050")
                .telefono("3001234567")
                .fechaNacimiento(LocalDate.of(2000, 7, 12))
                .salarioBase(new BigDecimal("2500000"))
                .clave("Clave123*")
                .idRol(3L);
    }

    @Test
    void aceptaUsuarioValido() {
        assertDoesNotThrow(() -> ValidadorUsuario.validarDatos(usuarioValido().build()));
    }

    @Test
    void rechazaCuerpoNulo() {
        assertThrows(DatosInvalidosException.class, () -> ValidadorUsuario.validarDatos(null));
    }

    @Test
    void rechazaCamposObligatoriosVacios() {
        User user = User.builder().nombres(" ").build();

        DatosInvalidosException ex = assertThrows(DatosInvalidosException.class, () -> ValidadorUsuario.validarDatos(user));

        assertEquals(6, ex.getErrores().size());
    }

    @Test
    void rechazaCorreoConFormatoInvalido() {
        User user = usuarioValido().correoElectronico("correo-malo").build();

        assertThrows(DatosInvalidosException.class, () -> ValidadorUsuario.validarDatos(user));
    }

    @Test
    void rechazaSalarioFueraDeRango() {
        User mayor = usuarioValido().salarioBase(new BigDecimal("15000001")).build();
        User negativo = usuarioValido().salarioBase(new BigDecimal("-1")).build();

        assertThrows(DatosInvalidosException.class, () -> ValidadorUsuario.validarDatos(mayor));
        assertThrows(DatosInvalidosException.class, () -> ValidadorUsuario.validarDatos(negativo));
    }

    @Test
    void aceptaSalarioEnLosLimites() {
        assertDoesNotThrow(() -> ValidadorUsuario.validarDatos(usuarioValido().salarioBase(BigDecimal.ZERO).build()));
        assertDoesNotThrow(() -> ValidadorUsuario.validarDatos(usuarioValido().salarioBase(new BigDecimal("15000000")).build()));
    }

    @Test
    void rechazaFechaNacimientoFutura() {
        User user = usuarioValido().fechaNacimiento(LocalDate.now().plusDays(1)).build();

        assertThrows(DatosInvalidosException.class, () -> ValidadorUsuario.validarDatos(user));
    }

    @Test
    void rechazaTelefonoInvalido() {
        User user = usuarioValido().telefono("12ab").build();

        assertThrows(DatosInvalidosException.class, () -> ValidadorUsuario.validarDatos(user));
    }

    @Test
    void rechazaDocumentoInvalido() {
        User user = usuarioValido().documentoIdentidad("12-3").build();

        assertThrows(DatosInvalidosException.class, () -> ValidadorUsuario.validarDatos(user));
    }

    @Test
    void permiteCamposOpcionalesVacios() {
        User user = usuarioValido().telefono(null).documentoIdentidad(" ").fechaNacimiento(null).build();

        assertDoesNotThrow(() -> ValidadorUsuario.validarDatos(user));
    }

    @Test
    void acumulaTodosLosErroresDeFormato() {
        User user = usuarioValido()
                .correoElectronico("correo-malo")
                .salarioBase(new BigDecimal("20000000"))
                .fechaNacimiento(LocalDate.now().plusYears(1))
                .telefono("12ab")
                .documentoIdentidad("12")
                .build();

        DatosInvalidosException ex = assertThrows(DatosInvalidosException.class, () -> ValidadorUsuario.validarDatos(user));

        assertEquals(5, ex.getErrores().size());
    }

    @Test
    void rechazaClaveCortaOLarga() {
        User corta = usuarioValido().clave("1234567").build();
        User larga = usuarioValido().clave("x".repeat(65)).build();

        assertThrows(DatosInvalidosException.class, () -> ValidadorUsuario.validarDatos(corta));
        assertThrows(DatosInvalidosException.class, () -> ValidadorUsuario.validarDatos(larga));
    }

    @Test
    void rechazaRolQueNoExiste() {
        User user = usuarioValido().idRol(9L).build();

        DatosInvalidosException ex = assertThrows(DatosInvalidosException.class, () -> ValidadorUsuario.validarDatos(user));

        assertEquals("El campo idRol debe ser 1 (administrador), 2 (asesor) o 3 (cliente)", ex.getErrores().get(0));
    }

    @Test
    void soloAdministradorOAsesorPuedenRegistrar() {
        assertDoesNotThrow(() -> ValidadorUsuario.validarPermisoDeRegistro(new UsuarioAutenticado(1L, "a@c.com", null, Rol.ADMIN)));
        assertDoesNotThrow(() -> ValidadorUsuario.validarPermisoDeRegistro(new UsuarioAutenticado(2L, "b@c.com", null, Rol.ASESOR)));
        assertThrows(AccesoDenegadoException.class,
                () -> ValidadorUsuario.validarPermisoDeRegistro(new UsuarioAutenticado(3L, "c@c.com", null, Rol.CLIENTE)));
        assertThrows(AccesoDenegadoException.class, () -> ValidadorUsuario.validarPermisoDeRegistro(null));
    }

    @Test
    void rechazaCorreoYDocumentoYaRegistrados() {
        User user = usuarioValido().build();

        assertThrows(CorreoYaRegistradoException.class, () -> ValidadorUsuario.validarCorreoDisponible(user, correo -> true));
        assertThrows(DocumentoYaRegistradoException.class, () -> ValidadorUsuario.validarDocumentoDisponible(user, doc -> true));
        assertDoesNotThrow(() -> ValidadorUsuario.validarCorreoDisponible(user, correo -> false));
    }

    @Test
    void sinDocumentoNoConsultaSiEstaRegistrado() {
        User sinDocumento = usuarioValido().documentoIdentidad(null).build();

        assertDoesNotThrow(() -> ValidadorUsuario.validarDocumentoDisponible(sinDocumento, doc -> {
            throw new AssertionError("no debía consultar");
        }));
    }
}
