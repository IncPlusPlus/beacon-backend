package io.github.incplusplus.beacon.centralidentityserver.mapper;

import io.github.incplusplus.beacon.centralidentityserver.generated.dto.NewCityDto;
import io.github.incplusplus.beacon.centralidentityserver.persistence.model.City;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CityMapper {

  NewCityDto cityToNewCityDto(City save);
}
