package io.github.incplusplus.beacon.centralidentityserver.security;

import io.github.incplusplus.beacon.centralidentityserver.persistence.dao.CityRepository;
import io.github.incplusplus.beacon.centralidentityserver.persistence.dao.UserRepository;
import io.github.incplusplus.beacon.centralidentityserver.persistence.model.City;
import io.github.incplusplus.beacon.centralidentityserver.persistence.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MyUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;
  private final CityRepository cityRepository;

  @Autowired
  public MyUserDetailsService(UserRepository userRepository, CityRepository cityRepository) {
    this.userRepository = userRepository;
    this.cityRepository = cityRepository;
  }

  private static List<GrantedAuthority> getAuthorities(List<String> roles) {
    List<GrantedAuthority> authorities = new ArrayList<>();
    for (String role : roles) {
      authorities.add(new SimpleGrantedAuthority(role));
    }
    return authorities;
  }

  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    /*
    Our two API consumers consist of regular users as well as various City instances.
    The cities have their own repository. We check both since this method isn't flexible
    enough to inform us if the current request's path starts with /city-cis-intercom
     */
    Optional<User> userOptional = userRepository.findByUsername(username);
    Optional<City> cityOptional = cityRepository.findById(username);
    if (userOptional.isEmpty() && cityOptional.isEmpty()) {
      throw new UsernameNotFoundException("No user found with username: " + username);
    }
    if (userOptional.isPresent()) {
      User user = userOptional.get();
      List<String> roles = List.of("ROLE_USER");
      return new org.springframework.security.core.userdetails.User(
          user.getUsername(), user.getPassword(), true, true, true, true, getAuthorities(roles));
    } else {
      // cityOptional is implied to be present. No check necessary.
      City city = cityOptional.get();
      List<String> roles = List.of("ROLE_CITY");

      return new org.springframework.security.core.userdetails.User(
          city.getId(), city.getPassword(), true, true, true, true, getAuthorities(roles));
    }
  }
}
