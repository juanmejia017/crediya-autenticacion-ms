package co.com.crediya.jpa.seed;

import co.com.crediya.jpa.entity.RolEntity;
import co.com.crediya.jpa.repository.RolJpaRepository;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatosInicialesTest {

    @Test
    void creaLosTresRolesSiNoExisten() {
        RolJpaRepository repository = mock(RolJpaRepository.class);
        when(repository.existsById(any())).thenReturn(false);

        new DatosIniciales(repository).run(null);

        verify(repository, times(3)).save(any(RolEntity.class));
    }

    @Test
    void noDuplicaLosRolesExistentes() {
        RolJpaRepository repository = mock(RolJpaRepository.class);
        when(repository.existsById(any())).thenReturn(true);

        new DatosIniciales(repository).run(null);

        verify(repository, never()).save(any(RolEntity.class));
    }
}
