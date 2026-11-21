package co.com.crediya.usecase.support;

import co.com.crediya.model.auth.Sesion;
import co.com.crediya.model.auth.UsuarioAutenticado;
import co.com.crediya.model.auth.gateways.TokenGateway;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Generador de tokens de mentira: guarda en memoria qué usuario corresponde a cada token. */
public class FakeTokenGateway implements TokenGateway {
    private final Map<String, UsuarioAutenticado> tokens = new HashMap<>();

    @Override
    public Sesion generar(UsuarioAutenticado usuario) {
        String token = "token-" + usuario.idUsuario();
        tokens.put(token, usuario);
        return new Sesion(token, Sesion.TIPO_BEARER, 3600, usuario);
    }

    @Override
    public Optional<UsuarioAutenticado> validar(String token) {
        return Optional.ofNullable(tokens.get(token));
    }
}
