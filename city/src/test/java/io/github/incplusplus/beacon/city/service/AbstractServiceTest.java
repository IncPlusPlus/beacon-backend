package io.github.incplusplus.beacon.city.service;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ComponentScan({"io.github.incplusplus.beacon.city.mapper"})
// https://stackoverflow.com/a/70047908/1687436
@TestPropertySource(properties = "spring.mongodb.embedded.version=4.0.2")
@ActiveProfiles("test")
public abstract class AbstractServiceTest {}
