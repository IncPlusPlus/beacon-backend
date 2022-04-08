package io.github.incplusplus.beacon.centralidentityserver.mapper;

import io.github.incplusplus.beacon.centralidentityserver.generated.dto.TowerInviteDto;
import io.github.incplusplus.beacon.centralidentityserver.persistence.model.TowerInvite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TowerInviteMapper {
  TowerInviteDto towerInviteToTowerInviteDto(TowerInvite invite);

  @Mapping(target = "id", ignore = true)
  TowerInvite towerInviteDtoToTowerInvite(TowerInviteDto dto);
}
