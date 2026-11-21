package co.com.crediya.model.auth;

import co.com.crediya.model.exceptions.TechnicalException;
import co.com.crediya.model.rol.Rol;
import co.com.crediya.model.user.User;

import java.util.Arrays;

/**
 * Quién hizo la petición, según su token: lo que el sistema necesita para decidir permisos.
 */
public record UsuarioAutenticado(Long idUsuario, String correoElectronico, String documentoIdentidad, Rol rol) {

    /**
     * Arma el usuario autenticado a partir del usuario guardado. Su rol debe ser uno de los del sistema.
     */
    public static UsuarioAutenticado desdeUsuario(User usuario) {
        Rol rol = Rol.porId(usuario.getIdRol())
                .orElseThrow(() -> new TechnicalException("ROL_NO_CONFIGURADO",
                        "El usuario " + usuario.getIdUsuario() + " no tiene un rol válido"));
        return new UsuarioAutenticado(
                usuario.getIdUsuario(), usuario.getCorreoElectronico(), usuario.getDocumentoIdentidad(), rol);
    }

    public boolean tieneAlgunRol(Rol... roles) {
        return Arrays.asList(roles).contains(rol);
    }
}
