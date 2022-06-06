package io.github.incplusplus.beacon.city.service;

import io.github.incplusplus.beacon.city.generated.dto.MessageDto;
import io.github.incplusplus.beacon.city.mapper.MessageMapper;
import io.github.incplusplus.beacon.city.persistence.dao.ChannelRepository;
import io.github.incplusplus.beacon.city.persistence.dao.MessageRepository;
import io.github.incplusplus.beacon.city.persistence.dao.TowerRepository;
import io.github.incplusplus.beacon.city.persistence.model.Message;
import io.github.incplusplus.beacon.city.security.LoginAuthenticationProvider;
import io.github.incplusplus.beacon.city.websocket.notifier.MessageNotifier;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MessageService {

  private final TowerRepository towerRepository;
  private final ChannelRepository channelRepository;
  private final MessageRepository messageRepository;
  private final MessageMapper messageMapper;
  private final LoginAuthenticationProvider loginAuthenticationProvider;
  private final MessageNotifier messageNotifier;

  @Autowired
  public MessageService(
      TowerRepository towerRepository,
      ChannelRepository channelRepository,
      MessageRepository messageRepository,
      MessageMapper messageMapper,
      LoginAuthenticationProvider loginAuthenticationProvider,
      MessageNotifier messageNotifier) {
    this.towerRepository = towerRepository;
    this.channelRepository = channelRepository;
    this.messageRepository = messageRepository;
    this.messageMapper = messageMapper;
    this.loginAuthenticationProvider = loginAuthenticationProvider;
    this.messageNotifier = messageNotifier;
  }

  public MessageDto createMessage(
      String senderUsername,
      String towerId,
      String channelId,
      List<MultipartFile> attachments,
      MessageDto messageDto) {
    if (!towerRepository.existsById(towerId)) {
      // TODO: Make a proper exception
      throw new RuntimeException("Tower not found");
    }
    if (!channelRepository.existsById(channelId)) {
      // TODO: Make a proper exception
      throw new RuntimeException("Channel not found");
    }
    // TODO: Probably should verify that the channel is actually one of the channels within the
    //  specified tower
    Message newMessage = messageMapper.messageDtoToMessage(messageDto);
    newMessage.setSentTime(Instant.now());
    newMessage.setTowerId(towerId);
    newMessage.setChannelId(channelId);
    newMessage.setSenderId(loginAuthenticationProvider.getIdForUsername(senderUsername));
    // TODO: Deal with saving attachments and generating the URLs for them.
    //  These URLs will then populate the array of attachment URLs in the entity.
    newMessage = messageRepository.save(newMessage);
    MessageDto newMessageDto = messageMapper.messageToMessageDto(newMessage);
    // Kick off any tasks needed here for the websocket notification about a new message
    messageNotifier.notifyNewMessage(newMessageDto);
    return newMessageDto;
  }

  public List<MessageDto> getMessages(String towerId, String channelId) {
    if (!towerRepository.existsById(towerId)) { // TODO: Make a proper exception
      throw new RuntimeException("Tower not found");
    }
    if (!channelRepository.existsById(channelId)) { // TODO: Make a proper exception
      throw new RuntimeException("Channel not found");
    }
    return messageRepository.findAllByTowerIdAndChannelId(towerId, channelId).stream()
        .map(messageMapper::messageToMessageDto)
        .collect(Collectors.toList());
  }

  public Optional<MessageDto> editMessage(
      String towerId, String channelId, String messageId, MessageDto messageDto) {
    if (!towerRepository.existsById(towerId)) {
      // TODO: Make a proper exception
      throw new RuntimeException("Tower not found");
    }
    if (!channelRepository.existsById(channelId)) {
      // TODO: Make a proper exception
      throw new RuntimeException("Channel not found");
    }
    Optional<Message> messageOptional = messageRepository.findById(messageId);
    if (messageOptional.isEmpty()) {
      return Optional.empty();
    }
    Message message = messageOptional.get();
    // Users may only edit the message body. We don't let them sneakily modify any other fields
    message.setMessageBody(messageDto.getMessageBody());
    message.setEdited(true);
    // Save the edited message and convert the resulting object into a DTO
    MessageDto editedDto = messageMapper.messageToMessageDto(messageRepository.save(message));
    // Notify all subscribed clients that this message has been edited
    messageNotifier.notifyEditedMessage(editedDto);
    // Return the DTO
    return Optional.of(editedDto);
  }

  public Optional<MessageDto> deleteMessage(String towerId, String channelId, String messageId) {
    if (!towerRepository.existsById(towerId)) {
      // TODO: Make a proper exception
      throw new RuntimeException("Tower not found");
    }
    if (!channelRepository.existsById(channelId)) {
      // TODO: Make a proper exception
      throw new RuntimeException("Channel not found");
    }
    Optional<MessageDto> deletedOptional =
        messageRepository.deleteMessageById(messageId).map(messageMapper::messageToMessageDto);
    // If it's not empty, we proceed to return the deleted message and dispatch the notification
    deletedOptional.ifPresent(messageNotifier::notifyDeletedMessage);
    return deletedOptional;
  }

  /**
   * For internal use only.
   *
   * @param channelId the channel id to look for when deleting messages
   */
  public void deleteAllByChannelId(String channelId) {
    messageRepository.deleteAllByChannelId(channelId);
  }
}
