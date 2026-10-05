package br.com.qualidade;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.*;

abstract class BaseTest {
    protected ChromeDriver driver;
    protected WebDriverWait wait;

    @BeforeEach
    void abrirChrome() {
        ChromeOptions options = new ChromeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        options.addArguments("--window-size=1440,1000", "--disable-notifications");
        if (Boolean.getBoolean("headless")) options.addArguments("--headless=new");
        driver = new ChromeDriver(options); // Selenium Manager gerencia o ChromeDriver.
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        wait = new WebDriverWait(driver, Duration.ofSeconds(25));
        // Isola anúncios de terceiros; não altera campos, validação ou respostas do site.
        driver.executeCdpCommand("Network.enable", Map.of());
        driver.executeCdpCommand("Network.setBlockedURLs", Map.of("urls", List.of(
            "*doubleclick.net/*", "*googlesyndication.com/*", "*googleadservices.com/*")));
        driver.get("https://automationexercise.com/");
        assertTrue(visivel(By.cssSelector("#slider")).isDisplayed(), "Home deve estar visível");
        assertEquals("Automation Exercise", driver.getTitle());
        clicar(By.cssSelector("a[href='/login']"));
        assertEquals("Login to your account", visivel(By.cssSelector(".login-form h2")).getText());
        assertEquals("New User Signup!", visivel(By.cssSelector(".signup-form h2")).getText());
    }

    protected By qa(String nome) { return By.cssSelector("[data-qa='" + nome + "']"); }
    protected WebElement visivel(By by) { return wait.until(ExpectedConditions.visibilityOfElementLocated(by)); }
    protected void preencher(By by, String valor) {
        WebElement campo = visivel(by);
        campo.clear();
        if (!valor.isEmpty()) campo.sendKeys(valor);
    }
    protected void clicar(By by) {
        WebElement elemento = wait.until(ExpectedConditions.elementToBeClickable(by));
        driver.executeScript("arguments[0].scrollIntoView({block:'center'});", elemento);
        elemento.click();
    }
    protected String emailUnico() { return "selenium." + UUID.randomUUID() + "@example.com"; }
    protected void evidencia(String nome) throws Exception {
        Path destino = Path.of("target", "evidencias", nome + ".png");
        Files.createDirectories(destino.getParent());
        Files.write(destino, driver.getScreenshotAs(OutputType.BYTES));
    }
    protected void validarBloqueio(By campo, String propriedade, String urlAntes) {
        assertEquals(Boolean.TRUE, driver.executeScript(
            "return arguments[0].validity[arguments[1]];", driver.findElement(campo), propriedade));
        assertEquals(Boolean.FALSE, driver.executeScript(
            "return arguments[0].validity.valid;", driver.findElement(campo)));
        assertEquals(urlAntes, driver.getCurrentUrl(), "Formulário inválido não deve navegar");
    }

    @AfterEach
    void fecharChrome(TestInfo info) throws Exception {
        if (driver != null) {
            try {
                evidencia(info.getTestMethod().orElseThrow().getName() + "-" + UUID.randomUUID());
            } finally {
                driver.quit();
            }
        }
    }
}
