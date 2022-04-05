package io.github.incplusplus.beacon.city.websocket.notifier;

import io.github.incplusplus.beacon.city.generated.dto.MessageDto;
import io.github.incplusplus.beacon.city.websocket.event.CrudEventType;
import io.github.incplusplus.beacon.city.websocket.event.MessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageNotifier {
  private final SimpMessagingTemplate broker;

  @Autowired
  public MessageNotifier(SimpMessagingTemplate broker) {
    this.broker = broker;
  }

  public void notifyNewMessage(MessageDto newMessageDto) {
    broker.convertAndSend(
        messageWebsocketDestination(newMessageDto.getTowerId(), newMessageDto.getChannelId()),
        new MessageEvent(CrudEventType.CREATED, newMessageDto.getId(), newMessageDto));
  }

  public void notifyEditedMessage(MessageDto editedMessageDto) {
    broker.convertAndSend(
        messageWebsocketDestination(editedMessageDto.getTowerId(), editedMessageDto.getChannelId()),
        new MessageEvent(CrudEventType.EDITED, editedMessageDto.getId(), editedMessageDto));
  }

  public void notifyDeletedMessage(MessageDto deletedMessageDto) {
    broker.convertAndSend(
        messageWebsocketDestination(
            deletedMessageDto.getTowerId(), deletedMessageDto.getChannelId()),
        new MessageEvent(CrudEventType.DELETED, deletedMessageDto.getId(), deletedMessageDto));
  }

  /**
   * Returns the "/messages" websocket destination for a given tower and channel
   *
   * @param towerId the id of the tower this message is in
   * @param channelId the id of the channel this message is in
   * @return /topic/tower/{tower-id}/channel/{channel-id}/message
   */
  private String messageWebsocketDestination(String towerId, String channelId) {
    return "/topic/tower/" + towerId + "/channel/" + channelId + "/message";
  }
}
