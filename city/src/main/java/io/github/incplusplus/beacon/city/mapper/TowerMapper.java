package io.github.incplusplus.beacon.city.mapper;

import io.github.incplusplus.beacon.city.generated.dto.TowerDto;
import io.github.incplusplus.beacon.city.persistence.model.Tower;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TowerMapper {

  @Mapping(target = "channels", ignore = true)
  Tower towerDtoToTower(TowerDto towerDto);

  TowerDto towerToTowerDto(Tower tower);
}
