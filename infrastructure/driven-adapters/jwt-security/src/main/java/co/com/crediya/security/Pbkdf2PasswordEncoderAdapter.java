package co.com.crediya.security;

import co.com.crediya.model.auth.gateways.PasswordEncoderGateway;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Encripta las claves con PBKDF2-HMAC-SHA256 y una sal aleatoria por usuario.
 * Se guarda "pbkdf2$iteraciones$sal$hash": la clave original nunca se guarda ni se puede recuperar.
 */
@Component
public class Pbkdf2PasswordEncoderAdapter implements PasswordEncoderGateway {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final String PREFIJO = "pbkdf2";
    private static final int ITERACIONES = 210_000;
    private static final int BYTES_SAL = 16;
    private static final int BITS_HASH = 256;

    private final SecureRandom random = new SecureRandom();

    @Override
    public String encriptar(String clave) {
        byte[] sal = new byte[BYTES_SAL];
        random.nextBytes(sal);
        byte[] hash = derivar(clave, sal, ITERACIONES);
        Base64.Encoder base64 = Base64.getEncoder();
        return String.join("$", PREFIJO, String.valueOf(ITERACIONES), base64.encodeToString(sal), base64.encodeToString(hash));
    }

    @Override
    public boolean coincide(String clave, String claveEncriptada) {
        if (clave == null || claveEncriptada == null) {
            return false;
        }
        String[] partes = claveEncriptada.split("\\$");
        if (partes.length != 4 || !PREFIJO.equals(partes[0])) {
            return false;
        }
        try {
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            return MessageDigest.isEqual(esperado, derivar(clave, sal, iteraciones));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derivar(String clave, byte[] sal, int iteraciones) {
        PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), sal, iteraciones, BITS_HASH);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No fue posible encriptar la clave", e);
        } finally {
            spec.clearPassword();
        }
    }
}
