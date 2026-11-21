package co.com.crediya.jpa.mapper;

import co.com.crediya.jpa.entity.UserEntity;
import co.com.crediya.model.rol.Rol;
import co.com.crediya.model.user.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserEntityMapperTest {

    private final User user = User.builder()
            .idUsuario(5L).nombres("Ana").apellidos("Gómez").fechaNacimiento(LocalDate.of(1998, 5, 20))
            .direccion("Carrera 45").telefono("3109876543").correoElectronico("ana@correo.com")
            .documentoIdentidad("1037654321").idRol(3L).salarioBase(new BigDecimal("3000000")).clave("pbkdf2$hash")
            .build();

    @Test
    void convierteElUsuarioEnEntidadConTodosLosCampos() {
        UserEntity entidad = UserEntityMapper.aEntidad(user);

        assertEquals(5L, entidad.getIdUsuario());
        assertEquals("ana@correo.com", entidad.getCorreoElectronico());
        assertEquals(3L, entidad.getIdRol());
        assertEquals("pbkdf2$hash", entidad.getClave());
        assertEquals(LocalDate.of(1998, 5, 20), entidad.getFechaNacimiento());
    }

    @Test
    void idaYVueltaConservaLosDatos() {
        User deVuelta = UserEntityMapper.aDominio(UserEntityMapper.aEntidad(user));

        assertEquals(user.getNombres(), deVuelta.getNombres());
        assertEquals(user.getDocumentoIdentidad(), deVuelta.getDocumentoIdentidad());
        assertEquals(user.getSalarioBase(), deVuelta.getSalarioBase());
        assertEquals(user.getClave(), deVuelta.getClave());
    }

    @Test
    void convierteElRolEnEntidad() {
        assertEquals("CLIENTE", RolEntityMapper.aEntidad(Rol.CLIENTE).getNombre());
        assertEquals(3L, RolEntityMapper.aEntidad(Rol.CLIENTE).getIdRol());
    }
}
