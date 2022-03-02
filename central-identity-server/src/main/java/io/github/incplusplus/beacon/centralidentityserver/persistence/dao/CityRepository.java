package io.github.incplusplus.beacon.centralidentityserver.persistence.dao;

import io.github.incplusplus.beacon.centralidentityserver.persistence.model.City;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface CityRepository extends CrudRepository<City, String> {

  Optional<City> findByBasePath(String cityBasePath);
}
