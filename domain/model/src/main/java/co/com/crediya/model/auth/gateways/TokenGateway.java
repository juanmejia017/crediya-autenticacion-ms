package co.com.crediya.model.auth.gateways;

import co.com.crediya.model.auth.Sesion;
import co.com.crediya.model.auth.UsuarioAutenticado;

import java.util.Optional;

public interface TokenGateway {
    Sesion generar(UsuarioAutenticado usuario);
    Optional<UsuarioAutenticado> validar(String token);
}
