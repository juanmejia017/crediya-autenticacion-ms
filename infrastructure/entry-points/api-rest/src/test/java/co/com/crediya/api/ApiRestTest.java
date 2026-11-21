package co.com.crediya.api;

import co.com.crediya.model.auth.UsuarioAutenticado;
import co.com.crediya.model.exceptions.AccesoDenegadoException;
import co.com.crediya.model.exceptions.CorreoYaRegistradoException;
import co.com.crediya.model.exceptions.DatosInvalidosException;
import co.com.crediya.model.exceptions.DocumentoYaRegistradoException;
import co.com.crediya.model.rol.Rol;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.autenticacion.AutenticacionUseCase;
import co.com.crediya.usecase.user.UserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = TestConfig.class)
class ApiRestTest {

    private static final String BODY = """
            {
              "nombres": "Juan",
              "apellidos": "Mejía",
              "fechaNacimiento": "2000-07-12",
              "direccion": "Calle 10 # 20-30",
              "telefono": "3001234567",
              "correoElectronico": "juan@correo.com",
              "salarioBase": 2500000,
              "idRol": 3,
              "clave": "Clave123*"
            }
            """;

    @Autowired
    private WebApplicationContext context;

    private static final String TOKEN = "Bearer token-admin";
    private static final UsuarioAutenticado ADMIN = new UsuarioAutenticado(1L, "admin@crediya.com", null, Rol.ADMIN);

    @MockitoBean
    private UserUseCase userUseCase;

    @MockitoBean
    private AutenticacionUseCase autenticacionUseCase;

    private RestTestClient client;

    @BeforeEach
    void setup() {
        client = RestTestClient.bindToApplicationContext(context).build();
        when(autenticacionUseCase.validarToken("token-admin")).thenReturn(ADMIN);
    }

    @Test
    void registraElUsuarioYResponde201() {
        User creado = User.builder()
                .idUsuario(1L).nombres("Juan").apellidos("Mejía")
                .correoElectronico("juan@correo.com").salarioBase(new BigDecimal("2500000"))
                .build();
        when(userUseCase.registrarUsuario(any(), eq(ADMIN))).thenReturn(creado);

        client.post().uri("/api/v1/usuarios")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BODY)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().value("Location", ubicacion -> assertTrue(ubicacion.endsWith("/api/v1/usuarios/1")))
                .expectBody()
                .jsonPath("$.idUsuario").isEqualTo(1);
    }

    @Test
    void responde400SiLosDatosSonInvalidos() {
        when(userUseCase.registrarUsuario(any(), eq(ADMIN)))
                .thenThrow(new DatosInvalidosException(List.of("El campo nombres es obligatorio")));

        client.post().uri("/api/v1/usuarios")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BODY)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.codigo").isEqualTo("DATOS_INVALIDOS");
    }

    @Test
    void responde409SiElCorreoYaExiste() {
        when(userUseCase.registrarUsuario(any(), eq(ADMIN)))
                .thenThrow(new CorreoYaRegistradoException());

        client.post().uri("/api/v1/usuarios")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BODY)
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void responde409SiElDocumentoYaExiste() {
        when(userUseCase.registrarUsuario(any(), eq(ADMIN))).thenThrow(new DocumentoYaRegistradoException());

        client.post().uri("/api/v1/usuarios")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BODY)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.codigo").isEqualTo("DOCUMENTO_YA_REGISTRADO");
    }

    @Test
    void responde400SiElJsonEstaMalFormado() {
        client.post().uri("/api/v1/usuarios")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{ \"salarioBase\": \"abc\" }")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.codigo").isEqualTo("JSON_INVALIDO");
    }

    @Test
    void responde415SiElContentTypeNoEsJson() {
        client.post().uri("/api/v1/usuarios")
                .contentType(MediaType.TEXT_PLAIN)
                .body("texto")
                .exchange()
                .expectStatus().isEqualTo(415)
                .expectBody()
                .jsonPath("$.codigo").isEqualTo("TIPO_CONTENIDO_NO_SOPORTADO");
    }

    @Test
    void responde500GenericoAnteUnErrorInesperado() {
        when(userUseCase.registrarUsuario(any(), eq(ADMIN))).thenThrow(new IllegalStateException("detalle interno"));

        client.post().uri("/api/v1/usuarios")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BODY)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.codigo").isEqualTo("ERROR_INTERNO");
    }

    @Test
    void responde401SiNoHayToken() {
        client.post().uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BODY)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().valueEquals("WWW-Authenticate", "Bearer")
                .expectBody()
                .jsonPath("$.codigo").isEqualTo("NO_AUTENTICADO");

        verify(userUseCase, never()).registrarUsuario(any(), any());
    }

    @Test
    void responde403SiNoEsAdministradorNiAsesor() {
        when(userUseCase.registrarUsuario(any(), eq(ADMIN)))
                .thenThrow(new AccesoDenegadoException("Solo un administrador o un asesor puede registrar usuarios"));

        client.post().uri("/api/v1/usuarios")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BODY)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.codigo").isEqualTo("ACCESO_DENEGADO");
    }

    @Test
    void noDevuelveLaClaveEnLaRespuesta() {
        User creado = User.builder()
                .idUsuario(1L).nombres("Juan").apellidos("Mejía").correoElectronico("juan@correo.com")
                .salarioBase(new BigDecimal("2500000")).clave("pbkdf2$hash").build();
        when(userUseCase.registrarUsuario(any(), eq(ADMIN))).thenReturn(creado);

        client.post().uri("/api/v1/usuarios")
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BODY)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.clave").doesNotExist();
    }
}
