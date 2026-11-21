package co.com.crediya.jpa.mapper;

import co.com.crediya.jpa.entity.UserEntity;
import co.com.crediya.model.user.User;

/**
 * Convierte entre el modelo del dominio (User) y la entidad de la base de datos (UserEntity).
 */
public final class UserEntityMapper {

    private UserEntityMapper() {
    }

    public static UserEntity aEntidad(User user) {
        return UserEntity.builder()
                .idUsuario(user.getIdUsuario())
                .nombres(user.getNombres())
                .apellidos(user.getApellidos())
                .fechaNacimiento(user.getFechaNacimiento())
                .direccion(user.getDireccion())
                .telefono(user.getTelefono())
                .correoElectronico(user.getCorreoElectronico())
                .documentoIdentidad(user.getDocumentoIdentidad())
                .idRol(user.getIdRol())
                .salarioBase(user.getSalarioBase())
                .clave(user.getClave())
                .build();
    }

    public static User aDominio(UserEntity entity) {
        return User.builder()
                .idUsuario(entity.getIdUsuario())
                .nombres(entity.getNombres())
                .apellidos(entity.getApellidos())
                .fechaNacimiento(entity.getFechaNacimiento())
                .direccion(entity.getDireccion())
                .telefono(entity.getTelefono())
                .correoElectronico(entity.getCorreoElectronico())
                .documentoIdentidad(entity.getDocumentoIdentidad())
                .idRol(entity.getIdRol())
                .salarioBase(entity.getSalarioBase())
                .clave(entity.getClave())
                .build();
    }
}
