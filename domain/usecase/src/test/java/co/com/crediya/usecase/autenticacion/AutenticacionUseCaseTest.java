package co.com.crediya.usecase.autenticacion;

import co.com.crediya.model.auth.Sesion;
import co.com.crediya.model.auth.UsuarioAutenticado;
import co.com.crediya.model.exceptions.CredencialesInvalidasException;
import co.com.crediya.model.exceptions.DatosInvalidosException;
import co.com.crediya.model.exceptions.NoAutenticadoException;
import co.com.crediya.model.exceptions.TechnicalException;
import co.com.crediya.model.rol.Rol;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.support.FakePasswordEncoder;
import co.com.crediya.usecase.support.FakeTokenGateway;
import co.com.crediya.usecase.support.FakeUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AutenticacionUseCaseTest {

    private FakeUserRepository repository;
    private AutenticacionUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new FakeUserRepository();
        useCase = new AutenticacionUseCase(repository, new FakePasswordEncoder(), new FakeTokenGateway());
        repository.guardar(User.builder()
                .nombres("Ana").apellidos("Cliente").correoElectronico("ana@correo.com")
                .documentoIdentidad("1020304050").salarioBase(new BigDecimal("2000000"))
                .idRol(Rol.CLIENTE.getId()).clave("hash:Clave123*")
                .build());
    }

    @Test
    void iniciaSesionConCorreoYClaveCorrectos() {
        Sesion sesion = useCase.iniciarSesion(" Ana@Correo.com ", "Clave123*");

        assertEquals("Bearer", sesion.tipo());
        assertEquals(Rol.CLIENTE, sesion.usuario().rol());
        assertEquals("1020304050", sesion.usuario().documentoIdentidad());
    }

    @Test
    void rechazaClaveIncorrecta() {
        assertThrows(CredencialesInvalidasException.class, () -> useCase.iniciarSesion("ana@correo.com", "otra-clave"));
    }

    @Test
    void rechazaCorreoQueNoExisteConElMismoError() {
        assertThrows(CredencialesInvalidasException.class, () -> useCase.iniciarSesion("nadie@correo.com", "Clave123*"));
    }

    @Test
    void noTieneLimiteDeIntentos() {
        for (int intento = 0; intento < 20; intento++) {
            assertThrows(CredencialesInvalidasException.class, () -> useCase.iniciarSesion("ana@correo.com", "mala"));
        }
        assertDoesNotThrow(() -> useCase.iniciarSesion("ana@correo.com", "Clave123*"));
    }

    @Test
    void exigeCorreoYClave() {
        DatosInvalidosException ex = assertThrows(DatosInvalidosException.class, () -> useCase.iniciarSesion(" ", null));

        assertEquals(2, ex.getErrores().size());
        assertEquals(0, repository.consultas);
    }

    @Test
    void usuarioSinRolValidoEsErrorTecnico() {
        repository.guardar(User.builder().correoElectronico("sinrol@correo.com").clave("hash:Clave123*").build());

        assertThrows(TechnicalException.class, () -> useCase.iniciarSesion("sinrol@correo.com", "Clave123*"));
    }

    @Test
    void validaElTokenDelHeaderAuthorization() {
        Sesion sesion = useCase.iniciarSesion("ana@correo.com", "Clave123*");

        UsuarioAutenticado usuario = useCase.validarToken("Bearer " + sesion.token());

        assertEquals("ana@correo.com", usuario.correoElectronico());
        assertEquals(Rol.CLIENTE, usuario.rol());
    }

    @Test
    void rechazaHeaderAusenteOSinBearer() {
        assertThrows(NoAutenticadoException.class, () -> useCase.validarToken(null));
        assertThrows(NoAutenticadoException.class, () -> useCase.validarToken("token-1"));
    }

    @Test
    void rechazaTokenInvalido() {
        assertThrows(NoAutenticadoException.class, () -> useCase.validarToken("Bearer inventado"));
    }
}
