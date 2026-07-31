package Modelo;

import java.util.regex.Pattern;

/**
 * Servicio de validación y saneamiento de datos de entrada para evitar inconsistencias y excepciones.
 */
public class InputValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    /**
     * Valida si la cadena es un correo electrónico válido.
     */
    public static boolean esEmailValido(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Valida si el texto puede convertirse a un número decimal no negativo.
     */
    public static boolean esPrecioValido(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return false;
        }
        try {
            double val = Double.parseDouble(texto.trim());
            return val >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Valida si el texto puede convertirse a un entero positivo.
     */
    public static boolean esEnteroPositivo(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return false;
        }
        try {
            int val = Integer.parseInt(texto.trim());
            return val > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Sanea una cadena quitando espacios innecesarios al inicio y final.
     */
    public static String sanearTexto(String texto) {
        return texto != null ? texto.trim() : "";
    }
}
