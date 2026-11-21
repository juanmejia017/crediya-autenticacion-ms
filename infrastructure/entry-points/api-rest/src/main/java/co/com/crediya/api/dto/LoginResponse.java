package co.com.crediya.api.dto;

import co.com.crediya.model.auth.Sesion;

public record LoginResponse(
        String token,
        String tipo,
        long expiraEnSegundos,
        Long idUsuario,
        String correoElectronico,
        String rol
) {
    public static LoginResponse fromDomain(Sesion sesion) {
        return new LoginResponse(
                sesion.token(),
                sesion.tipo(),
                sesion.expiraEnSegundos(),
                sesion.usuario().idUsuario(),
                sesion.usuario().correoElectronico(),
                sesion.usuario().rol().name()
        );
    }
}
