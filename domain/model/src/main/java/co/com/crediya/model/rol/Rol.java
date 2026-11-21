package co.com.crediya.model.rol;

import java.util.Arrays;
import java.util.Optional;

/**
 * Roles del sistema. El id coincide con la columna id_rol de la tabla roles.
 */
public enum Rol {
    ADMIN(1L, "Administrador"),
    ASESOR(2L, "Asesor"),
    CLIENTE(3L, "Cliente");

    private final Long id;
    private final String descripcion;

    Rol(Long id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    public Long getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static Optional<Rol> porId(Long id) {
        return Arrays.stream(values()).filter(rol -> rol.id.equals(id)).findFirst();
    }

    public static Optional<Rol> porNombre(String nombre) {
        return Arrays.stream(values()).filter(rol -> rol.name().equals(nombre)).findFirst();
    }
}
