package co.com.crediya.api;

import co.com.crediya.model.auth.Sesion;
import co.com.crediya.model.auth.UsuarioAutenticado;
import co.com.crediya.model.exceptions.CredencialesInvalidasException;
import co.com.crediya.model.exceptions.DatosInvalidosException;
import co.com.crediya.model.rol.Rol;
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

import java.util.List;

import static org.mockito.Mockito.when;

@SpringBootTest(classes = TestConfig.class)
class LoginApiRestTest {

    private static final String BODY = """
            { "correoElectronico": "ana@correo.com", "clave": "Clave123*" }
            """;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private AutenticacionUseCase autenticacionUseCase;

    @MockitoBean
    private UserUseCase userUseCase;

    private RestTestClient client;

    @BeforeEach
    void setup() {
        client = RestTestClient.bindToApplicationContext(context).build();
    }

    @Test
    void shouldReturnTokenWhenCredentialsAreValid() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(7L, "ana@correo.com", "1020304050", Rol.CLIENTE);
        when(autenticacionUseCase.iniciarSesion("ana@correo.com", "Clave123*"))
                .thenReturn(new Sesion("jwt-de-prueba", Sesion.TIPO_BEARER, 3600, usuario));

        client.post().uri("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BODY)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isEqualTo("jwt-de-prueba")
                .jsonPath("$.tipo").isEqualTo("Bearer")
                .jsonPath("$.expiraEnSegundos").isEqualTo(3600)
                .jsonPath("$.rol").isEqualTo("CLIENTE");
    }

    @Test
    void shouldReturn401WhenCredentialsAreInvalid() {
        when(autenticacionUseCase.iniciarSesion("ana@correo.com", "Clave123*"))
                .thenThrow(new CredencialesInvalidasException());

        client.post().uri("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BODY)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.codigo").isEqualTo("CREDENCIALES_INVALIDAS");
    }

    @Test
    void shouldReturn400WhenFieldsAreMissing() {
        when(autenticacionUseCase.iniciarSesion(null, null))
                .thenThrow(new DatosInvalidosException(List.of("El campo correoElectronico es obligatorio",
                        "El campo clave es obligatorio")));

        client.post().uri("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.codigo").isEqualTo("DATOS_INVALIDOS");
    }

    @Test
    void shouldReturn400WhenBodyIsMissing() {
        client.post().uri("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.codigo").isEqualTo("DATOS_INVALIDOS");
    }
}
