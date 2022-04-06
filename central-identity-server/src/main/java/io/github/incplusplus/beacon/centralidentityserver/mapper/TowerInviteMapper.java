package io.github.incplusplus.beacon.centralidentityserver.mapper;

import io.github.incplusplus.beacon.centralidentityserver.generated.dto.TowerInviteDto;
import io.github.incplusplus.beacon.centralidentityserver.persistence.model.TowerInvite;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TowerInviteMapper {
  TowerInviteDto towerInviteToTowerInviteDto(TowerInvite invite);

  TowerInvite towerInviteDtoToTowerInvite(TowerInviteDto dto);
}
