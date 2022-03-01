package io.github.incplusplus.beacon.city.mapper;

import io.github.incplusplus.beacon.city.generated.dto.ChannelDto;
import io.github.incplusplus.beacon.city.persistence.model.Channel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChannelMapper {

  @Mapping(target = "messages", ignore = true)
  Channel channelDtoToChannel(ChannelDto channelDto);

  ChannelDto channelToChannelDto(Channel newChannel);
}
