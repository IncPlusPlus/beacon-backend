package io.github.incplusplus.beacon.centralidentityserver.security;

import io.github.incplusplus.beacon.centralidentityserver.persistence.dao.UserRepository;
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

  @Autowired
  public MyUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  private static List<GrantedAuthority> getAuthorities(List<String> roles) {
    List<GrantedAuthority> authorities = new ArrayList<>();
    for (String role : roles) {
      authorities.add(new SimpleGrantedAuthority(role));
    }
    return authorities;
  }

  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Optional<User> userOptional = userRepository.findByUsername(username);
    if (userOptional.isEmpty()) {
      throw new UsernameNotFoundException("No user found with username: " + username);
    }
    User user = userOptional.get();
    boolean enabled = true;
    boolean accountNonExpired = true;
    boolean credentialsNonExpired = true;
    boolean accountNonLocked = true;
    List<String> roles = List.of("ROLE_USER");

    return new org.springframework.security.core.userdetails.User(
        user.getUsername(),
        user.getPassword(),
        enabled,
        accountNonExpired,
        credentialsNonExpired,
        accountNonLocked,
        getAuthorities(roles));
  }
}
