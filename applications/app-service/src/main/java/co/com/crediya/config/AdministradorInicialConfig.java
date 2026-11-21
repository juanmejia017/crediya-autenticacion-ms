package co.com.crediya.config;

import co.com.crediya.model.user.User;
import co.com.crediya.usecase.user.UserUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.math.BigDecimal;

/**
 * Crea el primer administrador al arrancar. Sin él nadie podría iniciar sesión,
 * porque solo un administrador o un asesor puede registrar usuarios.
 */
@Slf4j
@Configuration
public class AdministradorInicialConfig {

    @Bean
    @Order(2)
    public ApplicationRunner crearAdministradorInicial(UserUseCase userUseCase,
                                                       @Value("${admin.correo}") String correo,
                                                       @Value("${admin.clave}") String clave) {
        return args -> {
            if (userUseCase.existeUsuarioConCorreo(correo)) {
                log.info("El administrador inicial ya existe");
                return;
            }
            User admin = User.builder()
                    .nombres("Administrador")
                    .apellidos("CrediYa")
                    .correoElectronico(correo)
                    .salarioBase(BigDecimal.ZERO)
                    .clave(clave)
                    .build();
            userUseCase.crearAdministradorInicial(admin);
            log.info("Administrador inicial creado con el correo {}", correo);
        };
    }
}
