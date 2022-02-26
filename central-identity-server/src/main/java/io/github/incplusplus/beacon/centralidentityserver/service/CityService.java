package io.github.incplusplus.beacon.centralidentityserver.service;

import io.github.incplusplus.beacon.centralidentityserver.generated.dto.NewCityDto;
import io.github.incplusplus.beacon.centralidentityserver.mapper.CityMapper;
import io.github.incplusplus.beacon.centralidentityserver.persistence.dao.CityRepository;
import io.github.incplusplus.beacon.centralidentityserver.persistence.model.City;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class CityService {

  private final CityRepository cityRepository;

  private final PasswordEncoder passwordEncoder;

  private final CityMapper cityMapper;

  @Autowired
  public CityService(
      CityRepository cityRepository, PasswordEncoder passwordEncoder, CityMapper cityMapper) {
    this.cityRepository = cityRepository;
    this.passwordEncoder = passwordEncoder;
    this.cityMapper = cityMapper;
  }

  public NewCityDto registerNewCity(String scheme, String hostname, boolean localhost) {
    // Maybe perform an API test against the City in question before persisting anything?
    // Something for the future.
    final String cityBasePath = scheme + "://" + hostname + (localhost ? ":8080" : "");

    City city;
    // Check if there's an existing city with that URL in the DB.
    // If this city merely forgot its identity, we don't want to have it lose all its information.
    // So, we reset its password. This allows it to keep its ID in the DB instead of recreating it.
    Optional<City> existingOptional = cityRepository.findByBasePath(cityBasePath);
    if (existingOptional.isPresent()) {
      log.info("Updating city " + existingOptional.get().getId());
    } else {
      log.info("Registering a new city.");
    }
    city = existingOptional.orElseGet(City::new);
    city.setBasePath(cityBasePath);
    // I didn't bother generating a secure random password.
    // A UUID should work decently well for a password I suppose.
    String passwordRaw = UUID.randomUUID().toString();
    city.setPassword(passwordEncoder.encode(passwordRaw));
    NewCityDto newCity = cityMapper.cityToNewCityDto(cityRepository.save(city));
    // We don't want to send back the encoded password.
    // Otherwise, the City won't ever be able to authenticate properly
    newCity.setPassword(passwordRaw);
    log.debug("Successfully registered new City with ID '" + newCity.getId() + "'.");
    return newCity;
  }
}
