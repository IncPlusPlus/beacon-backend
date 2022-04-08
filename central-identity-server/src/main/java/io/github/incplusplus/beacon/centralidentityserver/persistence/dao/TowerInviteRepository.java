package io.github.incplusplus.beacon.centralidentityserver.persistence.dao;

import io.github.incplusplus.beacon.centralidentityserver.persistence.model.TowerInvite;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface TowerInviteRepository extends CrudRepository<TowerInvite, String> {
  List<TowerInvite> findAllByCityIdAndTowerId(String cityId, String towerId);

  Optional<TowerInvite> findByInviteCode(String inviteCode);

  boolean existsTowerInviteByInviteCode(String inviteCode);
}
