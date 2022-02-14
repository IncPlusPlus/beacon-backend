package io.github.incplusplus.beacon.centralidentityserver.mapper;

import io.github.incplusplus.beacon.centralidentityserver.generated.dto.CreateAccountRequestDto;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.UserAccountDto;
import io.github.incplusplus.beacon.centralidentityserver.persistence.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

  UserAccountDto userToUserDto(User user);

  @Mapping(target = "profilePictureUrl", ignore = true)
  @Mapping(target = "id", ignore = true)
  User createAccountRequestDtoToUser(CreateAccountRequestDto createAccountRequestDto);
}
