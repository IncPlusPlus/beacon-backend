package io.github.incplusplus.beacon.city.websocket.notifier;

import io.github.incplusplus.beacon.city.generated.dto.ChannelDto;
import io.github.incplusplus.beacon.city.websocket.event.ChannelEvent;
import io.github.incplusplus.beacon.city.websocket.event.CrudEventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChannelNotifier {
  private final SimpMessagingTemplate broker;

  @Autowired
  public ChannelNotifier(SimpMessagingTemplate broker) {
    this.broker = broker;
  }

  public void notifyNewChannel(ChannelDto channelDto) {
    broker.convertAndSend(
        channelWebsocketDestination(channelDto.getTowerId()),
        new ChannelEvent(CrudEventType.CREATED, channelDto.getId(), channelDto));
  }

  public void notifyEditedChannel(ChannelDto channelDto) {
    broker.convertAndSend(
        channelWebsocketDestination(channelDto.getTowerId()),
        new ChannelEvent(CrudEventType.EDITED, channelDto.getId(), channelDto));
  }

  public void notifyDeletedChannel(ChannelDto channelDto) {
    broker.convertAndSend(
        channelWebsocketDestination(channelDto.getTowerId()),
        new ChannelEvent(CrudEventType.DELETED, channelDto.getId(), channelDto));
  }

  private String channelWebsocketDestination(String towerId) {
    return "/topic/tower/" + towerId + "/channel";
  }
}
