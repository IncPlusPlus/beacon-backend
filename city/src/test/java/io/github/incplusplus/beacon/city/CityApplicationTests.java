package io.github.incplusplus.beacon.city;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.incplusplus.beacon.city.generated.dto.NewCityDto;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

// https://stackoverflow.com/a/70047908/1687436
@TestPropertySource(properties = "spring.mongodb.embedded.version=4.0.2")
@SpringBootTest
class CityApplicationTests {
  public static MockWebServer mockBackEnd;
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @BeforeAll
  static void setUp() throws IOException {
    NewCityDto newCityDto = new NewCityDto().id("testCityId").password("testCityPassword");
    mockBackEnd = new MockWebServer();
    mockBackEnd.start(9876);

    mockBackEnd.enqueue(
        new MockResponse()
            .setBody(MAPPER.writeValueAsString(newCityDto))
            .addHeader("Content-Type", "application/json"));
  }

  @AfterAll
  static void tearDown() throws IOException {
    mockBackEnd.shutdown();
  }

  @Test
  void contextLoads() {}
}
