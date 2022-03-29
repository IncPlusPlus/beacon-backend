package io.github.incplusplus.beacon.centralidentityserver.service;

import com.google.common.collect.Streams;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.CityDto;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.NewCityDto;
import io.github.incplusplus.beacon.centralidentityserver.mapper.CityMapper;
import io.github.incplusplus.beacon.centralidentityserver.persistence.dao.CityRepository;
import io.github.incplusplus.beacon.centralidentityserver.persistence.model.City;
import io.github.incplusplus.beacon.centralidentityserver.persistence.model.User;
import java.util.ArrayList;
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
  private final UserService userService;

  @Autowired
  public CityService(
      CityRepository cityRepository,
      PasswordEncoder passwordEncoder,
      CityMapper cityMapper,
      UserService userService) {
    this.cityRepository = cityRepository;
    this.passwordEncoder = passwordEncoder;
    this.cityMapper = cityMapper;
    this.userService = userService;
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
    /*
    This will be null in the case of a new City. However, if an old City lost its identity and
    tried to re-establish itself (like what happens with Heroku apps that have temporary storage
    which is wiped when they are suspended), we don't want to wipe out the existing members list.
     */
    if (city.getMemberUsers() == null) {
      // The City needs to have the members list initialized. Otherwise, it'll be null and cause
      // problems for us when we want to retrieve the list and modify it.
      city.setMemberUsers(new ArrayList<>());
    }
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

  public List<String> addCityMembers(String cityId, List<String> userIds) {
    Optional<City> cityOptional = cityRepository.findById(cityId);
    if (cityOptional.isEmpty()) {
      // TODO: Make a proper exception for this
      throw new RuntimeException("City not found.");
    }
    City city = cityOptional.get();
    city.getMemberUsers().addAll(userIds);
    return cityRepository.save(city).getMemberUsers();
  }

  public List<String> setCityMembers(String cityId, List<String> cityMembers) {
    Optional<City> cityOptional = cityRepository.findById(cityId);
    if (cityOptional.isEmpty()) {
      // TODO: Make a proper exception for this
      throw new RuntimeException("City not found.");
    }
    City city = cityOptional.get();
    city.setMemberUsers(cityMembers);
    return cityRepository.save(city).getMemberUsers();
  }

  public List<String> getCityMembers(String cityId) {
    Optional<City> cityOptional = cityRepository.findById(cityId);
    if (cityOptional.isEmpty()) {
      // TODO: Make a proper exception for this
      throw new RuntimeException("City not found.");
    }
    return cityOptional.get().getMemberUsers();
  }

  public void removeCityMembers(String cityId, List<String> userIds) {
    Optional<City> cityOptional = cityRepository.findById(cityId);
    if (cityOptional.isEmpty()) {
      // TODO: Make a proper exception for this
      throw new RuntimeException("City not found.");
    }
    City city = cityOptional.get();
    city.getMemberUsers().removeAll(userIds);
    cityRepository.save(city);
  }

  public List<CityDto> listCitiesMemberOf(String username) {
    // If we're already here, we know this username exists.
    // No need to bother with checking if the Optional exists.
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    User user = userService.getAccountByUsername(username).get();
    return cityRepository.findAllByMemberUsersContains(user.getId()).stream()
        .map(cityMapper::cityToCityDto)
        .toList();
  }
}
