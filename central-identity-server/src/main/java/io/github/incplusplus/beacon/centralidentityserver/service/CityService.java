package io.github.incplusplus.beacon.centralidentityserver.service;

import com.google.common.collect.Streams;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.CityDto;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.NewCityDto;
import io.github.incplusplus.beacon.centralidentityserver.mapper.CityMapper;
import io.github.incplusplus.beacon.centralidentityserver.persistence.dao.CityRepository;
import io.github.incplusplus.beacon.centralidentityserver.persistence.model.City;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
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

  public NewCityDto registerNewCity(String hostname) {
    boolean localhost = hostname.equals("localhost");
    // Assume port 8080 if doing local testing. Otherwise, this should ALWAYS be 443
    final String cityBasePath =
        (localhost ? "http" : "https") + "://" + hostname + (localhost ? ":8080" : "");

    // Maybe perform an API test against the City in question before persisting anything?
    // Something for the future.
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

  public List<CityDto> getCities() {
    return Streams.stream(cityRepository.findAll())
        .map(cityMapper::cityToCityDto)
        .collect(Collectors.toList());
  }

  public Optional<CityDto> getCity(String cityId) {
    return cityRepository.findById(cityId).map(cityMapper::cityToCityDto);
  }
}
