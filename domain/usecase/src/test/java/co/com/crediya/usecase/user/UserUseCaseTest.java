package co.com.crediya.usecase.user;

import co.com.crediya.model.auth.UsuarioAutenticado;
import co.com.crediya.model.exceptions.AccesoDenegadoException;
import co.com.crediya.model.exceptions.CorreoYaRegistradoException;
import co.com.crediya.model.exceptions.DatosInvalidosException;
import co.com.crediya.model.exceptions.DocumentoYaRegistradoException;
import co.com.crediya.model.rol.Rol;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.support.FakePasswordEncoder;
import co.com.crediya.usecase.support.FakeUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserUseCaseTest {

    private static final UsuarioAutenticado ADMIN = new UsuarioAutenticado(1L, "admin@crediya.com", null, Rol.ADMIN);
    private static final UsuarioAutenticado ASESOR = new UsuarioAutenticado(2L, "asesor@crediya.com", null, Rol.ASESOR);
    private static final UsuarioAutenticado CLIENTE = new UsuarioAutenticado(3L, "cliente@crediya.com", null, Rol.CLIENTE);

    private FakeUserRepository repository;
    private UserUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new FakeUserRepository();
        useCase = new UserUseCase(repository, new FakePasswordEncoder());
    }

    private User.UserBuilder usuarioValido() {
        return User.builder()
                .nombres(" Juan ")
                .apellidos("Mejia")
                .correoElectronico("Juan@Correo.com")
                .documentoIdentidad("1020304050")
                .telefono("3001234567")
                .fechaNacimiento(LocalDate.of(2000, 7, 12))
                .salarioBase(new BigDecimal("2500000"))
                .clave("Clave123*")
                .idRol(Rol.CLIENTE.getId());
    }

    @Test
    void administradorRegistraUsuarioValidoYNormalizaDatos() {
        User creado = useCase.registrarUsuario(usuarioValido().build(), ADMIN);

        assertNotNull(creado.getIdUsuario());
        assertEquals("juan@correo.com", creado.getCorreoElectronico());
        assertEquals("Juan", creado.getNombres());
    }

    @Test
    void asesorTambienPuedeRegistrar() {
        assertNotNull(useCase.registrarUsuario(usuarioValido().build(), ASESOR).getIdUsuario());
    }

    @Test
    void clienteNoPuedeRegistrarUsuarios() {
        assertThrows(AccesoDenegadoException.class, () -> useCase.registrarUsuario(usuarioValido().build(), CLIENTE));
        assertEquals(0, repository.consultas);
        assertTrue(repository.guardados.isEmpty());
    }

    @Test
    void sinSesionNoPuedeRegistrar() {
        assertThrows(AccesoDenegadoException.class, () -> useCase.registrarUsuario(usuarioValido().build(), null));
    }

    @Test
    void guardaLaClaveEncriptadaNuncaEnTextoPlano() {
        useCase.registrarUsuario(usuarioValido().build(), ADMIN);

        assertEquals("hash:Clave123*", repository.guardados.get(0).getClave());
    }

    @Test
    void noConsultaNiGuardaSiLosDatosSonInvalidos() {
        User invalido = usuarioValido().correoElectronico("correo-malo").build();

        assertThrows(DatosInvalidosException.class, () -> useCase.registrarUsuario(invalido, ADMIN));
        assertEquals(0, repository.consultas);
        assertTrue(repository.guardados.isEmpty());
    }

    @Test
    void guardaElUsuarioNormalizado() {
        useCase.registrarUsuario(usuarioValido().documentoIdentidad(" ").build(), ADMIN);

        User guardado = repository.guardados.get(0);
        assertEquals("juan@correo.com", guardado.getCorreoElectronico());
        assertNull(guardado.getDocumentoIdentidad());
    }

    @Test
    void rechazaCorreoYaRegistrado() {
        useCase.registrarUsuario(usuarioValido().build(), ADMIN);
        User mismoCorreo = usuarioValido().documentoIdentidad("99887766").build();

        assertThrows(CorreoYaRegistradoException.class, () -> useCase.registrarUsuario(mismoCorreo, ADMIN));
        assertEquals(1, repository.guardados.size());
    }

    @Test
    void rechazaDocumentoYaRegistrado() {
        useCase.registrarUsuario(usuarioValido().build(), ADMIN);
        User mismoDocumento = usuarioValido().correoElectronico("otro@correo.com").build();

        assertThrows(DocumentoYaRegistradoException.class, () -> useCase.registrarUsuario(mismoDocumento, ADMIN));
        assertEquals(1, repository.guardados.size());
    }

    @Test
    void creaAdministradorInicialUnaSolaVez() {
        User admin = usuarioValido().correoElectronico("admin@crediya.com").documentoIdentidad(null).idRol(null).build();

        assertTrue(useCase.crearAdministradorInicial(admin));
        assertFalse(useCase.crearAdministradorInicial(admin));
        assertEquals(1, repository.guardados.size());
        assertEquals(Rol.ADMIN.getId(), repository.guardados.get(0).getIdRol());
        assertEquals("hash:Clave123*", repository.guardados.get(0).getClave());
    }
}
