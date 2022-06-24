package io.github.incplusplus.beacon.city.service;

import io.github.incplusplus.beacon.city.generated.dto.ChannelDto;
import io.github.incplusplus.beacon.city.mapper.ChannelMapper;
import io.github.incplusplus.beacon.city.persistence.dao.ChannelRepository;
import io.github.incplusplus.beacon.city.persistence.dao.TowerRepository;
import io.github.incplusplus.beacon.city.persistence.model.Channel;
import io.github.incplusplus.beacon.city.websocket.notifier.ChannelNotifier;
import io.github.incplusplus.beacon.common.exception.EntityDoesNotExistException;
import java.util.List;
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
    if (!towerRepository.existsById(towerId)) {
      throw new EntityDoesNotExistException("Tower", towerId);
    }
    Channel newChannel = channelMapper.channelDtoToChannel(channelDto);
    newChannel.setTowerId(towerId);
    newChannel = channelRepository.save(newChannel);
    ChannelDto newChannelDto = channelMapper.channelToChannelDto(newChannel);
    channelNotifier.notifyNewChannel(newChannelDto);
    return newChannelDto;
  }

  public List<ChannelDto> getChannels(String towerId) {
    if (!towerRepository.existsById(towerId)) {
      throw new EntityDoesNotExistException("Tower", towerId);
    }
    return channelRepository.findAllByTowerId(towerId).stream()
        .map(channelMapper::channelToChannelDto)
        .toList();
  }

  @Transactional
  public ChannelDto deleteChannel(String towerId, String channelId) {
    if (!towerRepository.existsById(towerId)) {
      throw new EntityDoesNotExistException("Tower", towerId);
    }
    // If the Optional is empty, the channel never existed to begin with
    ChannelDto deleted =
        channelRepository
            .deleteChannelById(channelId)
            .map(channelMapper::channelToChannelDto)
            .orElseThrow(() -> new EntityDoesNotExistException("Channel", channelId));
    // Notify clients that the channel was deleted
    channelNotifier.notifyDeletedChannel(deleted);
    // Delete all messages that were in chis channel.
    messageService.deleteAllByChannelId(channelId);
    return deleted;
  }

  public ChannelDto editChannel(String towerId, String channelId, ChannelDto channelDto) {
    if (!towerRepository.existsById(towerId)) {
      throw new EntityDoesNotExistException("Tower", towerId);
    }
    Channel channel =
        channelRepository
            .findById(channelId)
            .orElseThrow(() -> new EntityDoesNotExistException("Channel", channelId));
    channel.setName(channelDto.getName());
    channel.setOrder(channelDto.getOrder());
    channel = channelRepository.save(channel);
    ChannelDto existingChannelDto = channelMapper.channelToChannelDto(channel);
    channelNotifier.notifyEditedChannel(existingChannelDto);
    return existingChannelDto;
  }
}
