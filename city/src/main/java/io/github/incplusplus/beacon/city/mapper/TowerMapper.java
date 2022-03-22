package io.github.incplusplus.beacon.city.mapper;

import io.github.incplusplus.beacon.city.generated.dto.TowerDto;
import io.github.incplusplus.beacon.city.persistence.model.Tower;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TowerMapper {

  @Mapping(target = "channels", ignore = true)
  Tower towerDtoToTower(TowerDto towerDto);

  /*
  TODO: The city ID is something that isn't persisted. Instead, It's retrieved from the IDENTITY file.
   Because of this, we will set the cityId field manually just before returning the DTO.
   Look into whether MapStruct will let us do this by having a second parameter to this method
   that allows for the manual specification of a field. If not, also see if we can provide an
   implementation for this method that uses as much of the automated part as possible and manually
   assigns the cityId with the second parameter. Until this is done, some of the endpoints will
   return Towers without a cityId which will probably cause issues with the frontend.
   */
  @Mapping(target = "cityId", ignore = true)
  TowerDto towerToTowerDto(Tower tower);
}
