package io.github.incplusplus.beacon.city.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;

public abstract class AbstractControllerTest {
  protected final MockMvc mockMvc;

  protected AbstractControllerTest(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  protected ResultActions assertConstraintViolation(RequestBuilder requestBuilder)
      throws Exception {
    return mockMvc
        .perform(requestBuilder)
        .andExpect(status().isBadRequest())
        .andExpect(header().string("Content-Type", is("application/problem+json")))
        .andExpect(
            jsonPath("$.type").value("https://zalando.github.io/problem/constraint-violation"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.title").value("Constraint Violation"))
        .andExpect(jsonPath("$.violations").isArray());
  }
}
