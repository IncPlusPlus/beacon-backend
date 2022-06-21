package io.github.incplusplus.beacon.city.websocket.notifier;

import io.github.incplusplus.beacon.city.generated.dto.TowerDto;
import io.github.incplusplus.beacon.city.websocket.event.CrudEventType;
import io.github.incplusplus.beacon.city.websocket.event.TowerEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class TowerNotifier {
  private final SimpMessagingTemplate broker;

  @Autowired
  public TowerNotifier(SimpMessagingTemplate broker) {
    this.broker = broker;
  }

  public void notifyEditedTower(TowerDto towerDto) {
    broker.convertAndSend(
        messageWebsocketDestination(towerDto.getId()),
        new TowerEvent(CrudEventType.EDITED, towerDto.getId(), towerDto));
  }

  /**
   * Returns the "/tower/{tower-id}" websocket destination for a given tower and channel
   *
   * @param towerId the id of the tower this message is in
   * @return /topic/tower/{tower-id}
   */
  private String messageWebsocketDestination(String towerId) {
    return "/topic/tower/" + towerId;
  }
}
