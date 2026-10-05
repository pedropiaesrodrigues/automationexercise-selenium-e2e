package br.com.qualidade;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;
import static org.junit.jupiter.api.Assertions.*;

class RegistroTest extends BaseTest {
    static Stream<Arguments> cadastrosValidos() {
        return Stream.of(
            Arguments.of("R01 - mínimo", "A", "x", "1", "1", "1900"),
            Arguments.of("R02 - mínimo mais um", "Ana", "xy", "2", "2", "1901"),
            Arguments.of("R03 - próximos dos máximos", "Teste Trinta", "Senha123!", "30", "11", "2020"),
            Arguments.of("R04 - máximos das listas", "Teste Selenium", "Senha123!", "31", "12", "2021"));
    }

    private void iniciarCadastro(String nome) {
        preencher(qa("signup-name"), nome);
        preencher(qa("signup-email"), emailUnico());
        clicar(qa("signup-button"));
        assertEquals("ENTER ACCOUNT INFORMATION",
            visivel(By.cssSelector(".login-form h2")).getText().toUpperCase(java.util.Locale.ROOT));
        assertEquals(nome, visivel(By.id("name")).getAttribute("value"));
    }

    private void preencherDados(String senha, String dia, String mes, String ano) {
        clicar(By.id("id_gender1"));
        preencher(By.id("password"), senha);
        new Select(visivel(By.id("days"))).selectByValue(dia);
        new Select(visivel(By.id("months"))).selectByValue(mes);
        new Select(visivel(By.id("years"))).selectByValue(ano);
        clicar(By.id("newsletter"));
        clicar(By.id("optin"));
        assertTrue(driver.findElement(By.id("newsletter")).isSelected());
        assertTrue(driver.findElement(By.id("optin")).isSelected());
        preencher(By.id("first_name"), "Teste");
        preencher(By.id("last_name"), "Selenium");
        preencher(By.id("company"), "Empresa Ficticia");
        preencher(By.id("address1"), "123 Example Street");
        preencher(By.id("address2"), "Apartment 1");
        new Select(visivel(By.id("country"))).selectByVisibleText("Canada");
        preencher(By.id("state"), "Ontario");
        preencher(By.id("city"), "Toronto");
        preencher(By.id("zipcode"), "M5V 1A1");
        preencher(By.id("mobile_number"), "4165550100");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cadastrosValidos")
    void registrarEExcluirUsuario(String caso, String nome, String senha,
                                  String dia, String mes, String ano) throws Exception {
        iniciarCadastro(nome);
        preencherDados(senha, dia, mes, ano);
        clicar(qa("create-account"));
        assertEquals("ACCOUNT CREATED!", visivel(qa("account-created")).getText());
        evidencia(caso.substring(0, 3) + "-criado");
        // Limpeza de conta de teste mesmo se a verificação de login falhar.
        try {
            clicar(qa("continue-button"));
            assertEquals("Logged in as " + nome,
                visivel(By.xpath("//a[contains(.,'Logged in as')]")).getText());
        } finally {
            // Se Continue não chegou à home, volta pela UI antes da exclusão.
            if (driver.findElements(By.cssSelector("a[href='/delete_account']")).isEmpty()) {
                driver.get("https://automationexercise.com/");
            }
            clicar(By.cssSelector("a[href='/delete_account']"));
            assertEquals("ACCOUNT DELETED!", visivel(qa("account-deleted")).getText());
            evidencia(caso.substring(0, 3) + "-excluido");
            clicar(qa("continue-button"));
            assertTrue(visivel(By.cssSelector("a[href='/login']")).isDisplayed());
        }
    }

    static Stream<Arguments> signupInvalido() {
        return Stream.of(
            Arguments.of("R05 - nome vazio", "", "teste@example.com", "signup-name", "valueMissing"),
            Arguments.of("R06 - email vazio", "Teste", "", "signup-email", "valueMissing"),
            Arguments.of("R07 - email malformado", "Teste", "sem-arroba", "signup-email", "typeMismatch"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("signupInvalido")
    void signupInvalidoBloqueado(String caso, String nome, String email, String campo, String propriedade) {
        preencher(qa("signup-name"), nome);
        preencher(qa("signup-email"), email);
        String url = driver.getCurrentUrl();
        clicar(qa("signup-button"));
        validarBloqueio(qa(campo), propriedade, url);
        assertTrue(visivel(By.cssSelector(".signup-form h2")).isDisplayed());
    }

    @Test
    void R08_senhaVaziaImpedeCriacao() {
        iniciarCadastro("Teste Sem Senha");
        preencherDados("", "15", "6", "1995");
        String url = driver.getCurrentUrl();
        clicar(qa("create-account"));
        validarBloqueio(By.id("password"), "valueMissing", url);
        assertTrue(driver.findElements(qa("account-created")).isEmpty());
    }
}
