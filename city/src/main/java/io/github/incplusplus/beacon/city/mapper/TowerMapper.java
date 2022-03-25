package io.github.incplusplus.beacon.city.mapper;

import io.github.incplusplus.beacon.city.generated.dto.TowerDto;
import io.github.incplusplus.beacon.city.persistence.model.Tower;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TowerMapper {

  @Mapping(target = "channels", ignore = true)
  Tower towerDtoToTower(TowerDto towerDto);

  /**
   * Map a Tower internal document to a Tower DTO.
   *
   * <p>The city ID is something that isn't persisted. Instead, It's retrieved from the IDENTITY
   * file. Because of this, we will set the cityId field manually just before returning the DTO.
   *
   * @param tower a Tower that is being transformed to a DTO
   * @param cityId the ID of the City this Tower belongs to (should always be THIS City's ID)
   * @return a TowerDto representing the Tower document
   * @see <a
   *     href="https://mapstruct.org/documentation/stable/reference/html/#mappings-with-several-source-parameters">MapStruct
   *     documentation on multiple parameters</a>
   */
  @Mapping(source = "cityId", target = "cityId")
  TowerDto towerToTowerDto(Tower tower, String cityId);
}
