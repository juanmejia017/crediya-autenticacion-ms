package co.com.crediya.jpa;

import co.com.crediya.jpa.repository.UserJpaRepository;
import co.com.crediya.jpa.entity.UserEntity;
import co.com.crediya.model.exceptions.CorreoYaRegistradoException;
import co.com.crediya.model.exceptions.DocumentoYaRegistradoException;
import co.com.crediya.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UserRepositoryAdapterTest {

    @Mock
    private UserJpaRepository repository;

    private UserRepositoryAdapter adapter;

    private User user;
    private UserEntity entity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new UserRepositoryAdapter(repository);

        user = User.builder()
                .nombres("Juan")
                .apellidos("Mejia")
                .correoElectronico("juan@correo.com")
                .salarioBase(new BigDecimal("2500000"))
                .build();

        entity = UserEntity.builder()
                .idUsuario(1L)
                .nombres("Juan")
                .apellidos("Mejia")
                .correoElectronico("juan@correo.com")
                .salarioBase(new BigDecimal("2500000"))
                .build();
    }

    @Test
    void guardaYDevuelveElUsuarioConId() {
        when(repository.save(any(UserEntity.class))).thenReturn(entity);

        User result = adapter.guardar(user);

        assertEquals(1L, result.getIdUsuario());
        assertEquals("juan@correo.com", result.getCorreoElectronico());
    }

    @Test
    void correoDuplicadoLanzaCorreoYaRegistrado() {
        when(repository.save(any(UserEntity.class))).thenThrow(violacion(UserEntity.UK_CORREO));

        assertThrows(CorreoYaRegistradoException.class, () -> adapter.guardar(user));
    }

    @Test
    void documentoDuplicadoLanzaDocumentoYaRegistrado() {
        when(repository.save(any(UserEntity.class))).thenThrow(violacion(UserEntity.UK_DOCUMENTO));

        assertThrows(DocumentoYaRegistradoException.class, () -> adapter.guardar(user));
    }

    @Test
    void otraViolacionDeIntegridadSePropaga() {
        when(repository.save(any(UserEntity.class))).thenThrow(violacion("otra_restriccion"));

        assertThrows(DataIntegrityViolationException.class, () -> adapter.guardar(user));
    }

    @Test
    void consultaSiExisteElCorreo() {
        when(repository.existsByCorreoElectronico("juan@correo.com")).thenReturn(true);

        assertTrue(adapter.existePorCorreoElectronico("juan@correo.com"));
    }

    @Test
    void consultaSiExisteElDocumento() {
        when(repository.existsByDocumentoIdentidad("1020304050")).thenReturn(true);

        assertTrue(adapter.existePorDocumentoIdentidad("1020304050"));
    }

    @Test
    void buscaPorCorreoEIncluyeLaClave() {
        entity.setClave("pbkdf2$hash");
        when(repository.findByCorreoElectronico("juan@correo.com")).thenReturn(Optional.of(entity));

        User encontrado = adapter.buscarPorCorreoElectronico("juan@correo.com").orElseThrow();

        assertEquals(1L, encontrado.getIdUsuario());
        assertEquals("pbkdf2$hash", encontrado.getClave());
    }

    @Test
    void buscarPorCorreoInexistenteDevuelveVacio() {
        when(repository.findByCorreoElectronico("nadie@correo.com")).thenReturn(Optional.empty());

        assertTrue(adapter.buscarPorCorreoElectronico("nadie@correo.com").isEmpty());
    }

    private static DataIntegrityViolationException violacion(String restriccion) {
        return new DataIntegrityViolationException("could not execute statement",
                new RuntimeException("ERROR: duplicate key value violates unique constraint \"" + restriccion + "\""));
    }
}
