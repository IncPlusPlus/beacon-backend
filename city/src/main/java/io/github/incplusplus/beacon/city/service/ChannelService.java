package io.github.incplusplus.beacon.city.service;

import io.github.incplusplus.beacon.city.generated.dto.ChannelDto;
import io.github.incplusplus.beacon.city.mapper.ChannelMapper;
import io.github.incplusplus.beacon.city.persistence.dao.ChannelRepository;
import io.github.incplusplus.beacon.city.persistence.dao.TowerRepository;
import io.github.incplusplus.beacon.city.persistence.model.Channel;
import io.github.incplusplus.beacon.city.websocket.notifier.ChannelNotifier;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChannelService {

  private final TowerRepository towerRepository;
  private final ChannelRepository channelRepository;
  private final ChannelMapper channelMapper;
  private final MessageService messageService;
  private final ChannelNotifier channelNotifier;

  @Autowired
  public ChannelService(
      TowerRepository towerRepository,
      ChannelRepository channelRepository,
      ChannelMapper channelMapper,
      MessageService messageService,
      ChannelNotifier channelNotifier) {
    this.towerRepository = towerRepository;
    this.channelRepository = channelRepository;
    this.channelMapper = channelMapper;
    this.messageService = messageService;
    this.channelNotifier = channelNotifier;
  }

  @Transactional
  public ChannelDto createChannel(String towerId, ChannelDto channelDto) {
    if (!towerRepository.existsById(towerId)) { // TODO: Make a proper exception
      throw new RuntimeException("Tower not found");
    }
    Channel newChannel = channelMapper.channelDtoToChannel(channelDto);
    newChannel.setTowerId(towerId);
    newChannel = channelRepository.save(newChannel);
    ChannelDto newChannelDto = channelMapper.channelToChannelDto(newChannel);
    channelNotifier.notifyNewChannel(newChannelDto);
    return newChannelDto;
  }

  public List<ChannelDto> getChannels(String towerId) {
    return channelRepository.findAllByTowerId(towerId).stream()
        .map(channelMapper::channelToChannelDto)
        .toList();
  }

  @Transactional
  public Optional<ChannelDto> deleteChannel(String towerId, String channelId) {
    if (!towerRepository.existsById(towerId)) { // TODO: Make a proper exception
      throw new RuntimeException("Tower not found");
    }
    Optional<ChannelDto> deleted =
        channelRepository.deleteChannelById(channelId).map(channelMapper::channelToChannelDto);
    deleted.ifPresent(channelNotifier::notifyDeletedChannel);
    // Delete all messages that were in chis channel.
    deleted.ifPresent(channelDto -> messageService.deleteAllByChannelId(channelId));
    return deleted;
  }

  public ChannelDto editChannel(String towerId, String channelId, ChannelDto channelDto) {
    if (!towerRepository.existsById(towerId)) {
      // TODO: Make a proper exception
      throw new RuntimeException("Tower not found");
    }
    if (!channelRepository.existsById(channelId)) {
      // TODO: Make a proper exception
      throw new RuntimeException("Channel not found");
    }
    Optional<Channel> channelOptional = channelRepository.findById(channelId);
    if (channelOptional.isEmpty()) {
      // TODO: Make a proper exception
      throw new RuntimeException("Channel not found");
    }
    Channel existingChannel = channelOptional.get();
    existingChannel.setName(channelDto.getName());
    existingChannel.setOrder(channelDto.getOrder());
    existingChannel = channelRepository.save(existingChannel);
    ChannelDto existingChannelDto = channelMapper.channelToChannelDto(existingChannel);
    channelNotifier.notifyEditedChannel(existingChannelDto);
    return existingChannelDto;
  }
}
