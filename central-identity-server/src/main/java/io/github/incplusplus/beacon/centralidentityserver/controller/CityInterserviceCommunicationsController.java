package io.github.incplusplus.beacon.centralidentityserver.controller;

import io.github.incplusplus.beacon.centralidentityserver.generated.controller.CityInterserviceCommunicationsApi;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.CreateTowerInviteRequestDto;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.NewCityDto;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.UserAccountDto;
import io.github.incplusplus.beacon.centralidentityserver.mapper.UserMapper;
import io.github.incplusplus.beacon.centralidentityserver.security.IAuthenticationFacade;
import io.github.incplusplus.beacon.centralidentityserver.service.CityService;
import io.github.incplusplus.beacon.centralidentityserver.service.UserService;
import java.util.List;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CityInterserviceCommunicationsController implements CityInterserviceCommunicationsApi {

  private final UserDetailsService userDetailsService;
  private final IAuthenticationFacade authenticationFacade;
  private final PasswordEncoder passwordEncoder;
  private final CityService cityService;
  private final UserService userService;
  private final UserMapper userMapper;

  @Autowired
  public CityInterserviceCommunicationsController(
      UserDetailsService userDetailsService,
      IAuthenticationFacade authenticationFacade,
      PasswordEncoder passwordEncoder,
      CityService cityService,
      UserService userService,
      UserMapper userMapper) {
    this.userDetailsService = userDetailsService;
    this.authenticationFacade = authenticationFacade;
    this.passwordEncoder = passwordEncoder;
    this.cityService = cityService;
    this.userService = userService;
    this.userMapper = userMapper;
  }

  @Override
  public ResponseEntity<List<String>> addCityMembers(List<String> newMembers) {
    return ResponseEntity.ok(
        cityService.addCityMembers(authenticationFacade.getAuthentication().getName(), newMembers));
  }

  @Override
  public ResponseEntity<String> generateTowerInvite(
      CreateTowerInviteRequestDto createTowerInviteRequestDto) {
    // TODO Implement this
    throw new NotImplementedException("Logic to generate Tower invites not implemented yet.");
  }

  @Override
  public ResponseEntity<List<String>> getCityMembers() {
    return ResponseEntity.ok(
        cityService.getCityMembers(authenticationFacade.getAuthentication().getName()));
  }

  @Override
  public ResponseEntity<NewCityDto> registerCity(String cityHostName) {
    NewCityDto registered = cityService.registerNewCity(cityHostName);
    return new ResponseEntity<>(registered, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<Void> removeCityMembers(List<String> userIds) {
    cityService.removeCityMembers(authenticationFacade.getAuthentication().getName(), userIds);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<List<String>> setCityMembers(List<String> cityMembers) {
    return new ResponseEntity<>(
        cityService.setCityMembers(authenticationFacade.getAuthentication().getName(), cityMembers),
        HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<UserAccountDto> verifyUser(String username, String password) {
    UserDetails userDetails;
    UserAccountDto userAccountDto;
    try {
      // TODO: Why the hell did I do all this logic in the controller? Move this to a service at
      //  some point.
      // TODO In the future, check these for null. Otherwise, a 500 happens.
      userDetails = userDetailsService.loadUserByUsername(username);
    } catch (UsernameNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
    if (passwordEncoder.matches(password, userDetails.getPassword())) {
      // We already performed a loadByUsername, so it should be pretty safe to get w/o isPresent()
      //noinspection OptionalGetWithoutIsPresent
      userAccountDto = userMapper.userToUserDto(userService.getAccountByUsername(username).get());
      return ResponseEntity.ok(userAccountDto);
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }
}
