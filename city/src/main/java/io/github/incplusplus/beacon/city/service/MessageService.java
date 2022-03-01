package io.github.incplusplus.beacon.city.service;

import com.google.common.collect.Streams;
import io.github.incplusplus.beacon.city.generated.dto.MessageDto;
import io.github.incplusplus.beacon.city.mapper.MessageMapper;
import io.github.incplusplus.beacon.city.persistence.dao.ChannelRepository;
import io.github.incplusplus.beacon.city.persistence.dao.MessageRepository;
import io.github.incplusplus.beacon.city.persistence.dao.TowerRepository;
import io.github.incplusplus.beacon.city.persistence.model.Message;
import io.github.incplusplus.beacon.city.security.LoginAuthenticationProvider;
import java.time.Instant;
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

  @Autowired
  public MessageService(
      TowerRepository towerRepository,
      ChannelRepository channelRepository,
      MessageRepository messageRepository,
      MessageMapper messageMapper,
      LoginAuthenticationProvider loginAuthenticationProvider) {
    this.towerRepository = towerRepository;
    this.channelRepository = channelRepository;
    this.messageRepository = messageRepository;
    this.messageMapper = messageMapper;
    this.loginAuthenticationProvider = loginAuthenticationProvider;
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
    // TODO: Kick off any tasks needed here for the websocket notification about a new message
    return messageMapper.messageToMessageDto(newMessage);
  }

  public List<MessageDto> getMessages(String towerId, String channelId) {
    return Streams.stream(messageRepository.findAll())
        .map(messageMapper::messageToMessageDto)
        .collect(Collectors.toList());
  }
}
