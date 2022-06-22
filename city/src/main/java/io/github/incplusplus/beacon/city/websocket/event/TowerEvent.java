package io.github.incplusplus.beacon.city.websocket.event;

import io.github.incplusplus.beacon.city.generated.dto.TowerDto;
import lombok.Data;

@Data
public class TowerEvent {
  private final CrudEventType type;
  private final String id;
  private final TowerDto message;
}
