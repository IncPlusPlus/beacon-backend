package io.github.incplusplus.beacon.city.spring;

import static io.github.incplusplus.beacon.city.service.CisCommunicationsService.CIS_REGISTER_CITY_ENDPOINT;

import io.github.incplusplus.beacon.city.generated.dto.NewCityDto;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class AutoRegisterCity implements ApplicationListener<ContextRefreshedEvent> {
  private static final String CITY_ID_PROP_NAME = "cityId";
  private static final String CITY_CIS_PW_PROP_NAME = "cityCISPassword";
  private boolean alreadyRegistered = false;
  private final File identityFile =
      Paths.get("city-private-storage")
          .resolve(Paths.get("IDENTITY-FILE"))
          .normalize()
          .toAbsolutePath()
          .toFile();

  @Value("${city.cis-url:http://localhost:9876}")
  private String CIS_URL;

  @Value("${city.hostname}")
  private String hostName;

  private String cityId;
  private String cityCISPassword;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    if (alreadyRegistered) {
      return;
    }

    // TODO: Even if the IDENTITY file exists, we should still check with the CIS if these
    // credentials
    //  are still valid.
    log.info("Checking if this City has been assigned an identity...");
    // Check if saved ID and password exist locally
    if (identityFile.exists()) {
      log.info("Identity exists at '" + identityFile.getAbsolutePath() + "'. Reading...");
      loadProperties();
    } else {
      // Time to acquire a new identity
      log.info(
          "This City does not have an identity. Registering this City with the CIS at '"
              + CIS_URL
              + "' to obtain a new one.");
      establishIdentity();
      loadProperties();
    }
    alreadyRegistered = true;
    // TODO: More checks could happen here if we want more stability. For instance, the City never
    //  checks if the CIS_URL works unless it needs to reestablish its identity. If we want startup
    //  to fail faster if the CIS is down, we could double-check the identity if the file already
    //  exists.
  }

  public String getCISUrl() {
    return CIS_URL;
  }

  public String getCityId() {
    return cityId;
  }

  public String getCityCISPassword() {
    return cityCISPassword;
  }

  public String getHostName() {
    return hostName;
  }

  private void loadProperties() {
    Properties props = new Properties();
    try (FileInputStream in = new FileInputStream(identityFile)) {
      props.load(in);
    } catch (IOException e) {
      log.error(
          "Failed to load City's identity from file '" + identityFile.getAbsolutePath() + "'.", e);
    }
    this.cityId = props.getProperty(CITY_ID_PROP_NAME);
    this.cityCISPassword = props.getProperty(CITY_CIS_PW_PROP_NAME);
  }

  private void establishIdentity() {
    // Ensure the parent folders for the identity file exist (otherwise there will be an IOException
    // when writing the file)
    //noinspection ResultOfMethodCallIgnored
    identityFile.getParentFile().mkdirs();
    // Set up a WebClient
    WebClient client = WebClient.create(CIS_URL);
    // Obtain an identity
    NewCityDto credentials =
        client
            .post()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path(CIS_REGISTER_CITY_ENDPOINT)
                        .queryParam("city-host-name", getHostName())
                        .build())
            .retrieve()
            .bodyToMono(NewCityDto.class)
            .block(Duration.ofSeconds(30));
    Properties props = new Properties();
    props.setProperty(CITY_ID_PROP_NAME, credentials.getId());
    props.setProperty(CITY_CIS_PW_PROP_NAME, credentials.getPassword());
    try (FileOutputStream fos = new FileOutputStream(identityFile)) {
      props.store(fos, "City CIS Identity");
    } catch (IOException e) {
      log.error("Failed to save the City's new identity.", e);
    }
  }
}
