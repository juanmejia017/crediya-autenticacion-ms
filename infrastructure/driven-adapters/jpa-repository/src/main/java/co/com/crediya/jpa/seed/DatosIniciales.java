package co.com.crediya.jpa.seed;

import co.com.crediya.jpa.mapper.RolEntityMapper;
import co.com.crediya.jpa.repository.RolJpaRepository;
import co.com.crediya.model.rol.Rol;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Al arrancar, guarda en la tabla roles los roles del enum Rol que todavía no existan.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DatosIniciales implements ApplicationRunner {

    private final RolJpaRepository rolRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (Rol rol : Rol.values()) {
            if (!rolRepository.existsById(rol.getId())) {
                rolRepository.save(RolEntityMapper.aEntidad(rol));
                log.info("Rol inicial creado: {}", rol);
            }
        }
    }
}
