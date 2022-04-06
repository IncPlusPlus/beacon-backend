package io.github.incplusplus.beacon.centralidentityserver.service;

import io.github.incplusplus.beacon.centralidentityserver.generated.dto.TowerInviteDto;
import io.github.incplusplus.beacon.centralidentityserver.mapper.TowerInviteMapper;
import io.github.incplusplus.beacon.centralidentityserver.persistence.dao.TowerInviteRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TowerInviteService {
  private final TowerInviteRepository inviteRepository;
  private final TowerInviteMapper inviteMapper;

  @Autowired
  public TowerInviteService(
      TowerInviteRepository inviteRepository, TowerInviteMapper inviteMapper) {
    this.inviteRepository = inviteRepository;
    this.inviteMapper = inviteMapper;
  }

  public Optional<TowerInviteDto> getInvite(String towerInviteCode) {
    return inviteRepository
        .findByInviteCode(towerInviteCode)
        .map(inviteMapper::towerInviteToTowerInviteDto);
  }
}
