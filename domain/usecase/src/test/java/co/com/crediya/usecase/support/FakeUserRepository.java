package co.com.crediya.usecase.support;

import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FakeUserRepository implements UserRepository {
    public final List<User> guardados = new ArrayList<>();
    public int consultas;

    @Override
    public User guardar(User user) {
        User guardado = user.toBuilder().idUsuario((long) guardados.size() + 1).build();
        guardados.add(guardado);
        return guardado;
    }

    @Override
    public boolean existePorCorreoElectronico(String correoElectronico) {
        consultas++;
        return guardados.stream().anyMatch(u -> correoElectronico.equals(u.getCorreoElectronico()));
    }

    @Override
    public boolean existePorDocumentoIdentidad(String documentoIdentidad) {
        consultas++;
        return guardados.stream().anyMatch(u -> documentoIdentidad.equals(u.getDocumentoIdentidad()));
    }

    @Override
    public Optional<User> buscarPorCorreoElectronico(String correoElectronico) {
        consultas++;
        return guardados.stream().filter(u -> correoElectronico.equals(u.getCorreoElectronico())).findFirst();
    }
}
