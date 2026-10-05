package br.com.qualidade;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import static org.junit.jupiter.api.Assertions.*;

class LoginTest extends BaseTest {
    static Stream<Arguments> credenciaisInexistentes() {
        return Stream.of(
            Arguments.of("L01 - credenciais inexistentes", "SenhaIncorreta123!"),
            Arguments.of("L02 - senha de 1 caractere", "x"),
            Arguments.of("L03 - senha de 2 caracteres", "xy"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("credenciaisInexistentes")
    void loginIncorretoExibeErro(String caso, String senha) {
        preencher(qa("login-email"), emailUnico());
        preencher(qa("login-password"), senha);
        clicar(qa("login-button"));
        assertEquals("Your email or password is incorrect!",
            visivel(By.cssSelector(".login-form form p")).getText());
        assertTrue(driver.findElements(By.cssSelector("a[href='/logout']")).isEmpty());
    }

    static Stream<Arguments> entradasInvalidas() {
        return Stream.of(
            Arguments.of("L04 - email vazio", "", "Senha123!", "login-email", "valueMissing"),
            Arguments.of("L05 - email sem arroba", "email-invalido", "Senha123!", "login-email", "typeMismatch"),
            Arguments.of("L06 - email sem domínio", "usuario@", "Senha123!", "login-email", "typeMismatch"),
            Arguments.of("L07 - senha vazia", "teste@example.com", "", "login-password", "valueMissing"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("entradasInvalidas")
    void loginInvalidoBloqueadoPeloNavegador(String caso, String email, String senha,
                                            String campo, String propriedade) {
        preencher(qa("login-email"), email);
        preencher(qa("login-password"), senha);
        String url = driver.getCurrentUrl();
        clicar(qa("login-button"));
        validarBloqueio(qa(campo), propriedade, url);
        assertTrue(driver.findElements(By.cssSelector(".login-form form p")).isEmpty());
    }
}
