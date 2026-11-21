package co.com.crediya.model.auth.gateways;

public interface PasswordEncoderGateway {
    String encriptar(String clave);
    boolean coincide(String clave, String claveEncriptada);
}
