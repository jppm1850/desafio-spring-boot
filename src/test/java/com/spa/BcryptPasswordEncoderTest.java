package com.spa;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class BcryptPasswordEncoderTest {

    @Test
    public void generateEncodedPassword() {
        // Crear una instancia de BCryptPasswordEncoder
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        // Contraseñas a codificar
        String rawPassword1 = "admin123";
        String rawPassword2 = "password123";

        // Codificar y mostrar las contraseñas
        String encodedPassword1 = encoder.encode(rawPassword1);
        String encodedPassword2 = encoder.encode(rawPassword2);

        System.out.println("Contraseña codificada para 'admin123': " + encodedPassword1);
        System.out.println("Contraseña codificada para 'password123': " + encodedPassword2);

        // Verificar que la codificación funciona correctamente
        boolean matches1 = encoder.matches(rawPassword1, encodedPassword1);
        boolean matches2 = encoder.matches(rawPassword2, encodedPassword2);

        System.out.println("¿La contraseña 'admin123' coincide? " + matches1);
        System.out.println("¿La contraseña 'password123' coincide? " + matches2);
    }
}
