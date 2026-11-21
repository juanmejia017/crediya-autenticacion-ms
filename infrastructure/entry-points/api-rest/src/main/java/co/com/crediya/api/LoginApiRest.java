package co.com.crediya.api;

import co.com.crediya.api.dto.LoginRequest;
import co.com.crediya.api.dto.LoginResponse;
import co.com.crediya.model.auth.Sesion;
import co.com.crediya.model.exceptions.DatosInvalidosException;
import co.com.crediya.usecase.autenticacion.AutenticacionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = LoginApiRest.BASE_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class LoginApiRest {

    static final String BASE_PATH = "/api/v1/login";

    private final AutenticacionUseCase autenticacionUseCase;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> iniciarSesion(@RequestBody(required = false) LoginRequest request) {
        if (request == null) {
            throw new DatosInvalidosException(List.of("El cuerpo de la petición es obligatorio"));
        }
        log.info("POST {} - intento de inicio de sesión", BASE_PATH);
        Sesion sesion = autenticacionUseCase.iniciarSesion(request.correoElectronico(), request.clave());
        log.info("POST {} - sesión iniciada para el usuario {} con rol {}", BASE_PATH,
                sesion.usuario().idUsuario(), sesion.usuario().rol());
        return ResponseEntity.ok(LoginResponse.fromDomain(sesion));
    }
}
