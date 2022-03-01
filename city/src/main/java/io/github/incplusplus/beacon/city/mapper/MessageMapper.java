package io.github.incplusplus.beacon.city.mapper;

import io.github.incplusplus.beacon.city.generated.dto.MessageDto;
import io.github.incplusplus.beacon.city.persistence.model.Message;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MessageMapper {

  Message messageDtoToMessage(MessageDto messageDto);

  MessageDto messageToMessageDto(Message newMessage);
}
