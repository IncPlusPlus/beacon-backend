package io.github.incplusplus.beacon.city.persistence.dao;

import io.github.incplusplus.beacon.city.persistence.model.Tower;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface TowerRepository extends CrudRepository<Tower, String> {
  List<Tower> findAllByMemberAccountIdsContains(String accountId);

  boolean existsTowerByMemberAccountIdsContains(String accountId);

  int countTowersByMemberAccountIdsContains(String accountId);

  List<Tower> findTowersByIdIsNot(String towerId);
}
