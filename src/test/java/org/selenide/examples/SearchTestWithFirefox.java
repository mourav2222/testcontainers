package org.selenide.examples;

import com.codeborne.selenide.WebDriverRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.CollectionCondition.sizeGreaterThanOrEqual;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.sleep;
import static org.selenide.examples.Abi.*;
import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL;

@Testcontainers
public class SearchTestWithFirefox {
  @Container
  public BrowserWebDriverContainer browser =
    new BrowserWebDriverContainer(firefoxImage())
      .withRecordingMode(RECORD_ALL, new File("build"))
      .withCapabilities(new FirefoxOptions());

  @BeforeEach
  public void setUp() {
    RemoteWebDriver driver = browser.getWebDriver();
    System.out.println(browser.getVncAddress());
    WebDriverRunner.setWebDriver(driver);
  }

  @AfterEach
  public void tearDown() {
    WebDriverRunner.closeWebDriver();
  }

  @Test
  public void showSelenideUsers() {
    open("https://selenide.org/users.html");
    $("h3").shouldHave(text("Selenide users"));
    $$("#user-tags .tag").shouldHave(sizeGreaterThan(10));

    int usersize = $$("#selenide-users .user").filter(visible).size();
    System.out.println(String.format("Users size: %d", usersize));
    $$("#selenide-users .user").filter(visible)
      .shouldHave(sizeGreaterThanOrEqual(20));

    showUsersByTag("usa", 20);
    showUsersByTag("europe", 16);
    showUsersByTag("estonia", 14);
    showUsersByTag("ukraine", 6);

    sleep(1000);
  }

}
