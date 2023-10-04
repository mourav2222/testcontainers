package org.selenide.examples;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static com.codeborne.selenide.WebDriverRunner.hasWebDriverStarted;
import static org.selenide.examples.Abi.chromeImage;
import static org.selenide.examples.Abi.showUsersByTag;
import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL;

@Testcontainers
public class SearchTestWithSelenoid {

//  @Container
//  public BrowserWebDriverContainer chrome =
//    new BrowserWebDriverContainer(chromeImage())
//      .withRecordingMode(RECORD_ALL, new File("build"))
//      .withCapabilities(new ChromeOptions());

  private static WebDriver driver;

  Network network = Network.newNetwork();

  @Container
  public GenericContainer selenoid = new GenericContainer
    (DockerImageName.parse("aerokube/selenoid:1.11.0"))
    .withClasspathResourceMapping("browsers.json", "/etc/selenoid/browsers.json", BindMode.READ_ONLY)
    .withFileSystemBind("/var/run/docker.sock", "/var/run/docker.sock", BindMode.READ_WRITE)
    .withNetwork(network)
    .withNetworkAliases("selenoid")
    .withCommand("-conf /etc/selenoid/browsers.json -limit 4 -video-output-dir /opt/selenoid/video -log-output-dir /opt/selenoid/logs -container-network " + network.getId())
    .withExposedPorts(4444);

  @Container
  public GenericContainer selenoidui = new GenericContainer
    (DockerImageName.parse("aerokube/selenoid-ui:1.10.10"))
    .withNetwork(network)
    .withNetworkAliases("selenoid-ui")
    .dependsOn(selenoid)
    .withCommand("--selenoid-uri http://selenoid:4444" )
    .withExposedPorts(8080);

  @BeforeEach
  public void setUp() {

    Integer firstMappedPort = selenoid.getFirstMappedPort();
    String address = selenoid.getHost() + ":" + selenoid.getMappedPort(4444);

    String addressui = selenoidui.getHost() + ":" + selenoidui.getMappedPort(8080);
    System.out.println("Selenoid-UI: http://" + addressui);

//    String commandui = "--selenoid-uri http://" + selenoid.getCurrentContainerInfo().getConfig().getHostName() + ":4444";
//    String commandui = "--selenoid-uri http://" + selenoid.getHost() + ":4444";

//    System.out.println("Command ui: " + commandui);
//    selenoidui.setCommand(commandui);
//    selenoidui.stop();
//    selenoidui.start();
//
//    addressui = selenoidui.getHost() + ":" + selenoidui.getMappedPort(8080);
//    System.out.println("Selenoid-UI: http://" + addressui);

    Configuration.remote = "http://" + address + "/wd/hub";
    System.out.println(String.format("Selenoid remote: %s", Configuration.remote));

    Map<String, Boolean> options = new HashMap<>();
    options.put("enableVNC", true);
    options.put("enableVideo", true);
    options.put("enableLog", true);

    ChromeOptions capabilities = new ChromeOptions();
    capabilities.setBrowserVersion("117.0");
    Configuration.browserCapabilities = capabilities;
    Configuration.browserCapabilities.setCapability("selenoid:options", options);

//    RemoteWebDriver driver = chrome.getWebDriver();
//    WebDriverRunner.setWebDriver(driver);
  }

  @AfterEach
  public void tearDown() {

    if (hasWebDriverStarted()) {
      WebDriverRunner.closeWebDriver();
    }
//    WebDriverRunner.closeWebDriver();
  }

  @Test
  void showSelenideUsers() {
    open("https://selenide.org/users.html");
    driver = WebDriverRunner.getWebDriver();

    System.out.println("URL: " + WebDriverRunner.url());

    $("h3").shouldHave(text("Selenide users"));
    $$("#user-tags .tag").shouldHave(sizeGreaterThan(10));

    showUsersByTag("usa", 20);
    showUsersByTag("europe", 16);
    showUsersByTag("estonia", 14);
    showUsersByTag("ukraine", 6);

    sleep(1000);
  }
}
