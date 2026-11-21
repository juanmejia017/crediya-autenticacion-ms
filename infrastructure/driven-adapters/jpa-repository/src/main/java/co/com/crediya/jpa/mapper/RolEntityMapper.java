package co.com.crediya.jpa.mapper;

import co.com.crediya.jpa.entity.RolEntity;
import co.com.crediya.model.rol.Rol;

/**
 * Convierte un Rol del dominio en la entidad de la tabla roles.
 */
public final class RolEntityMapper {

    private RolEntityMapper() {
    }

    public static RolEntity aEntidad(Rol rol) {
        return RolEntity.builder()
                .idRol(rol.getId())
                .nombre(rol.name())
                .descripcion(rol.getDescripcion())
                .build();
    }
}
