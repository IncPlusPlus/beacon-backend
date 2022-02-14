package io.github.incplusplus.beacon.centralidentityserver.persistence.dao;

import io.github.incplusplus.beacon.centralidentityserver.persistence.model.User;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String> {

  Optional<User> findByEmailAddress(String emailAddress);

  Optional<User> findByUsername(String username);
}
