package co.com.crediya.usecase.support;

import co.com.crediya.model.auth.gateways.PasswordEncoderGateway;

/** Encriptador de mentira para las pruebas: antepone "hash:" a la clave. */
public class FakePasswordEncoder implements PasswordEncoderGateway {
    @Override
    public String encriptar(String clave) {
        return "hash:" + clave;
    }

    @Override
    public boolean coincide(String clave, String claveEncriptada) {
        return ("hash:" + clave).equals(claveEncriptada);
    }
}
