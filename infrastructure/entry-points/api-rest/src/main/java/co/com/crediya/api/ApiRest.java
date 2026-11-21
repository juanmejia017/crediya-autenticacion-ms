package co.com.crediya.api;

import co.com.crediya.api.dto.RegistrarUsuarioRequest;
import co.com.crediya.api.dto.UsuarioResponse;
import co.com.crediya.api.validacion.ValidadorPeticion;
import co.com.crediya.model.auth.UsuarioAutenticado;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.user.UserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Entrada HTTP del registro de usuarios: recibe la petición, la delega y arma la respuesta.
 */
@RestController
@RequestMapping(value = "/api/v1/usuarios", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ApiRest {

    private final UserUseCase userUseCase;
    private final ValidadorPeticion validadorPeticion;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioResponse> registrarUsuario(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody(required = false) RegistrarUsuarioRequest request) {
        UsuarioAutenticado solicitante = validadorPeticion.validarPeticionAutenticada(authorization, request);
        User creado = userUseCase.registrarUsuario(request.aDominio(), solicitante);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.getIdUsuario()).toUri();
        return ResponseEntity.created(ubicacion).body(UsuarioResponse.desdeDominio(creado));
    }
}
