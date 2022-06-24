package io.github.incplusplus.beacon.city.service;

import io.github.incplusplus.beacon.city.generated.dto.MessageDto;
import io.github.incplusplus.beacon.city.mapper.MessageMapper;
import io.github.incplusplus.beacon.city.persistence.dao.ChannelRepository;
import io.github.incplusplus.beacon.city.persistence.dao.MessageRepository;
import io.github.incplusplus.beacon.city.persistence.dao.TowerRepository;
import io.github.incplusplus.beacon.city.persistence.model.Message;
import io.github.incplusplus.beacon.city.security.LoginAuthenticationProvider;
import io.github.incplusplus.beacon.city.websocket.notifier.MessageNotifier;
import io.github.incplusplus.beacon.common.exception.EntityDoesNotExistException;
import io.github.incplusplus.beacon.common.exception.StorageException;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
  private final StorageService storageService;

  @Autowired
  public MessageService(
      TowerRepository towerRepository,
      ChannelRepository channelRepository,
      MessageRepository messageRepository,
      MessageMapper messageMapper,
      LoginAuthenticationProvider loginAuthenticationProvider,
      MessageNotifier messageNotifier,
      StorageService storageService) {
    this.towerRepository = towerRepository;
    this.channelRepository = channelRepository;
    this.messageRepository = messageRepository;
    this.messageMapper = messageMapper;
    this.loginAuthenticationProvider = loginAuthenticationProvider;
    this.messageNotifier = messageNotifier;
    this.storageService = storageService;
  }

  public MessageDto createMessage(
      String senderUsername,
      String towerId,
      String channelId,
      List<MultipartFile> attachments,
      MessageDto messageDto) {
    if (!towerRepository.existsById(towerId)) {
      throw new EntityDoesNotExistException("Tower", towerId);
    }
    if (!channelRepository.existsById(channelId)) {
      throw new EntityDoesNotExistException("Channel", channelId);
    }
    // Grab the sender's ID
    String senderId = loginAuthenticationProvider.getIdForUsername(senderUsername);
    // TODO: Probably should verify that the channel is actually one of the channels within the
    //  specified tower
    Message newMessage = messageMapper.messageDtoToMessage(messageDto);
    newMessage.setSentTime(Instant.now());
    newMessage.setTowerId(towerId);
    newMessage.setChannelId(channelId);
    newMessage.setSenderId(senderId);
    // Upload the attachments and grab their URLs
    List<String> attachmentUrls;
    if (attachments != null) {
      attachmentUrls =
          attachments.stream()
              .map(
                  multipartFile -> {
                    try {
                      return storageService.saveUserAttachment(
                          multipartFile, towerId, channelId, senderId);
                    } catch (IOException e) {
                      throw new StorageException(e);
                    }
                  })
              .toList();
    } else {
      attachmentUrls = new ArrayList<>();
    }

    // Add the attachment URLs to the message object before persisting it.
    newMessage.setAttachments(attachmentUrls);
    newMessage = messageRepository.save(newMessage);
    MessageDto newMessageDto = messageMapper.messageToMessageDto(newMessage);
    // Kick off any tasks needed here for the websocket notification about a new message
    messageNotifier.notifyNewMessage(newMessageDto);
    return newMessageDto;
  }

  public List<MessageDto> getMessages(String towerId, String channelId) {
    if (!towerRepository.existsById(towerId)) {
      throw new EntityDoesNotExistException("Tower", towerId);
    }
    if (!channelRepository.existsById(channelId)) {
      throw new EntityDoesNotExistException("Channel", channelId);
    }
    return messageRepository.findAllByTowerIdAndChannelId(towerId, channelId).stream()
        .map(messageMapper::messageToMessageDto)
        .collect(Collectors.toList());
  }

  public MessageDto editMessage(
      String towerId, String channelId, String messageId, MessageDto messageDto) {
    if (!towerRepository.existsById(towerId)) {
      throw new EntityDoesNotExistException("Tower", towerId);
    }
    if (!channelRepository.existsById(channelId)) {
      throw new EntityDoesNotExistException("Channel", channelId);
    }
    Message message =
        messageRepository
            .findById(messageId)
            .orElseThrow(() -> new EntityDoesNotExistException("Message", messageId));
    // Users may only edit the message body. We don't let them sneakily modify any other fields
    message.setMessageBody(messageDto.getMessageBody());
    message.setEdited(true);
    // Save the edited message and convert the resulting object into a DTO
    MessageDto editedDto = messageMapper.messageToMessageDto(messageRepository.save(message));
    // Notify all subscribed clients that this message has been edited
    messageNotifier.notifyEditedMessage(editedDto);
    // Return the DTO
    return editedDto;
  }

  public MessageDto deleteMessage(String towerId, String channelId, String messageId) {
    if (!towerRepository.existsById(towerId)) {
      throw new EntityDoesNotExistException("Tower", towerId);
    }
    if (!channelRepository.existsById(channelId)) {
      throw new EntityDoesNotExistException("Channel", channelId);
    }
    // Get the value from the Optional and throw if the message by that ID doesn't exist
    MessageDto deleted =
        messageRepository
            .deleteMessageById(messageId)
            .map(messageMapper::messageToMessageDto)
            .orElseThrow(() -> new EntityDoesNotExistException("Message", messageId));
    // Notify all clients that this message was deleted
    messageNotifier.notifyDeletedMessage(deleted);
    return deleted;
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
