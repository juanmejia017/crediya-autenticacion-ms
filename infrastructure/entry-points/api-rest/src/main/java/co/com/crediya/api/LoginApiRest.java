package co.com.crediya.api;

import co.com.crediya.api.dto.LoginRequest;
import co.com.crediya.api.dto.LoginResponse;
import co.com.crediya.api.validacion.ValidadorPeticion;
import co.com.crediya.model.auth.Sesion;
import co.com.crediya.usecase.autenticacion.AutenticacionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entrada HTTP del inicio de sesión: recibe correo y clave, lo delega y responde con el token.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/login", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class LoginApiRest {

    private final AutenticacionUseCase autenticacionUseCase;
    private final ValidadorPeticion validadorPeticion;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> iniciarSesion(@RequestBody(required = false) LoginRequest request) {
        validadorPeticion.validarCuerpo(request);
        log.info("Intento de inicio de sesión");

        Sesion sesion = autenticacionUseCase.iniciarSesion(request.correoElectronico(), request.clave());

        log.info("Sesión iniciada para el usuario {} con rol {}", sesion.usuario().idUsuario(), sesion.usuario().rol());
        return ResponseEntity.ok(LoginResponse.desdeDominio(sesion));
    }
}
