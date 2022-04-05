package io.github.incplusplus.beacon.city.websocket.event;

import io.github.incplusplus.beacon.city.generated.dto.ChannelDto;
import lombok.Data;

@Data
public class ChannelEvent {
  private final CrudEventType type;
  private final String id;
  private final ChannelDto channel;
}
