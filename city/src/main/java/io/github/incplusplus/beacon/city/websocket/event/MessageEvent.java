package io.github.incplusplus.beacon.city.websocket.event;

import io.github.incplusplus.beacon.city.generated.dto.MessageDto;
import lombok.Data;

@Data
public class MessageEvent {
  private final CrudEventType type;
  private final String id;
  private final MessageDto message;
}
