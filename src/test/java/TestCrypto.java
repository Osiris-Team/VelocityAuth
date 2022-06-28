import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestCrypto {
    @Test
    void test() {
        String hello = "hello";
        String encodedPassword = new Pbkdf2PasswordEncoder().encode(hello);
        assertTrue(new Pbkdf2PasswordEncoder().matches(hello, encodedPassword));
    }
}
