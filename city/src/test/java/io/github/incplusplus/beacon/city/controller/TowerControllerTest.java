package io.github.incplusplus.beacon.city.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.incplusplus.beacon.city.security.AuthenticationFacade;
import io.github.incplusplus.beacon.city.service.CisCommunicationsService;
import io.github.incplusplus.beacon.city.service.TowerService;
import io.github.incplusplus.beacon.city.spring.AutoRegisterCity;
import io.github.incplusplus.beacon.common.exception.EntityDoesNotExistException;
import javax.servlet.ServletContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.zalando.problem.spring.web.autoconfigure.ProblemJacksonAutoConfiguration;

@WebMvcTest(
    value = TowerController.class,
    excludeAutoConfiguration = ErrorMvcAutoConfiguration.class)
// https://github.com/zalando/problem-spring-web/issues/334
@Import({ProblemJacksonAutoConfiguration.class, AuthenticationFacade.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class TowerControllerTest extends AbstractControllerTest {
  @Autowired private WebApplicationContext webApplicationContext;

  @MockBean private TowerService towerService;

  @MockBean private AutoRegisterCity autoRegisterCity;

  @MockBean private CisCommunicationsService cisCommunicationsService;

  @Autowired
  public TowerControllerTest(MockMvc mockMvc) {
    super(mockMvc);
  }

  @Test
  public void givenWac_whenServletContext_thenItProvidesTowerController() {
    ServletContext servletContext = webApplicationContext.getServletContext();

    assertNotNull(servletContext);
    assertTrue(servletContext instanceof MockServletContext);
    assertNotNull(webApplicationContext.getBean("towerController"));
  }

  @Test
  @WithMockUser
  void testCheckIfMemberOfTower() throws Exception {
    // Test for nonexistent Tower
    doThrow(new EntityDoesNotExistException("Tower", "foo")).when(towerService).getTower(eq("foo"));
    mockMvc.perform(get("/towers/foo")).andExpect(status().isNotFound());
    verify(towerService, times(1)).getTower(eq("foo"));

    // Test behavior for returning a sample tower properly
  }
}
