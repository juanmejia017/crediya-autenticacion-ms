package co.com.crediya.model.user;

import co.com.crediya.model.auth.UsuarioAutenticado;
import co.com.crediya.model.exceptions.AccesoDenegadoException;
import co.com.crediya.model.exceptions.CorreoYaRegistradoException;
import co.com.crediya.model.exceptions.DatosInvalidosException;
import co.com.crediya.model.exceptions.DocumentoYaRegistradoException;
import co.com.crediya.model.rol.Rol;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Validaciones del usuario: permiso para registrar, formato de los datos y que el correo
 * y el documento no estén ya registrados. El caso de uso solo las llama en orden.
 */
public final class ValidadorUsuario {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern TELEFONO_PATTERN = Pattern.compile("^\\+?\\d{7,15}$");
    private static final Pattern DOCUMENTO_PATTERN = Pattern.compile("^[A-Za-z0-9]{5,20}$");
    private static final BigDecimal SALARIO_MINIMO = BigDecimal.ZERO;
    private static final BigDecimal SALARIO_MAXIMO = new BigDecimal("15000000");
    private static final int CLAVE_MINIMO = 8;
    private static final int CLAVE_MAXIMO = 64;

    private ValidadorUsuario() {
    }

    /**
     * Solo un administrador o un asesor que inició sesión puede registrar usuarios.
     */
    public static void validarPermisoDeRegistro(UsuarioAutenticado solicitante) {
        if (solicitante == null || !solicitante.tieneAlgunRol(Rol.ADMIN, Rol.ASESOR)) {
            throw new AccesoDenegadoException("Solo un administrador o un asesor puede registrar usuarios");
        }
    }

    /**
     * El correo no puede estar registrado. Recibe la consulta del repositorio como función.
     */
    public static void validarCorreoDisponible(User user, Predicate<String> existeCorreo) {
        if (existeCorreo.test(user.getCorreoElectronico())) {
            throw new CorreoYaRegistradoException();
        }
    }

    /**
     * Si viene documento, no puede estar registrado. Sin documento no se consulta.
     */
    public static void validarDocumentoDisponible(User user, Predicate<String> existeDocumento) {
        if (user.getDocumentoIdentidad() != null && existeDocumento.test(user.getDocumentoIdentidad())) {
            throw new DocumentoYaRegistradoException();
        }
    }

    /**
     * Formato de los datos: obligatorios, correo, salario, clave, rol y opcionales.
     */
    public static void validarDatos(User user) {
        if (user == null) {
            throw new DatosInvalidosException(List.of("El cuerpo de la petición es obligatorio"));
        }
        List<String> errores = new ArrayList<>();
        validarObligatorios(user, errores);
        validarOpcionales(user, errores);
        if (!errores.isEmpty()) {
            throw new DatosInvalidosException(errores);
        }
    }

    private static void validarObligatorios(User user, List<String> errores) {
        if (estaVacio(user.getNombres())) {
            errores.add("El campo nombres es obligatorio");
        }
        if (estaVacio(user.getApellidos())) {
            errores.add("El campo apellidos es obligatorio");
        }
        if (estaVacio(user.getCorreoElectronico())) {
            errores.add("El campo correoElectronico es obligatorio");
        } else if (!EMAIL_PATTERN.matcher(user.getCorreoElectronico().trim()).matches()) {
            errores.add("El campo correoElectronico no tiene un formato válido");
        }
        if (user.getSalarioBase() == null) {
            errores.add("El campo salarioBase es obligatorio");
        } else if (user.getSalarioBase().compareTo(SALARIO_MINIMO) < 0
                || user.getSalarioBase().compareTo(SALARIO_MAXIMO) > 0) {
            errores.add("El campo salarioBase debe estar entre 0 y 15000000");
        }
        if (estaVacio(user.getClave())) {
            errores.add("El campo clave es obligatorio");
        } else if (user.getClave().length() < CLAVE_MINIMO || user.getClave().length() > CLAVE_MAXIMO) {
            errores.add("El campo clave debe tener entre 8 y 64 caracteres");
        }
        if (user.getIdRol() == null) {
            errores.add("El campo idRol es obligatorio");
        } else if (Rol.porId(user.getIdRol()).isEmpty()) {
            errores.add("El campo idRol debe ser 1 (administrador), 2 (asesor) o 3 (cliente)");
        }
    }

    private static void validarOpcionales(User user, List<String> errores) {
        if (user.getFechaNacimiento() != null && !user.getFechaNacimiento().isBefore(LocalDate.now())) {
            errores.add("El campo fechaNacimiento debe ser una fecha pasada");
        }
        if (!estaVacio(user.getTelefono()) && !TELEFONO_PATTERN.matcher(user.getTelefono().trim()).matches()) {
            errores.add("El campo telefono debe tener entre 7 y 15 dígitos (puede iniciar con +)");
        }
        if (!estaVacio(user.getDocumentoIdentidad())
                && !DOCUMENTO_PATTERN.matcher(user.getDocumentoIdentidad().trim()).matches()) {
            errores.add("El campo documentoIdentidad debe tener entre 5 y 20 caracteres alfanuméricos");
        }
    }

    private static boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
