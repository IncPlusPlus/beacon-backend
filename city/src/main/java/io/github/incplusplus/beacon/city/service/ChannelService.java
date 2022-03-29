package io.github.incplusplus.beacon.city.service;

import com.google.common.collect.Streams;
import io.github.incplusplus.beacon.city.generated.dto.ChannelDto;
import io.github.incplusplus.beacon.city.mapper.ChannelMapper;
import io.github.incplusplus.beacon.city.persistence.dao.ChannelRepository;
import io.github.incplusplus.beacon.city.persistence.dao.TowerRepository;
import io.github.incplusplus.beacon.city.persistence.model.Channel;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChannelService {

  private final TowerRepository towerRepository;
  private final ChannelRepository channelRepository;
  private final ChannelMapper channelMapper;
  private final MessageService messageService;

  @Autowired
  public ChannelService(
      TowerRepository towerRepository,
      ChannelRepository channelRepository,
      ChannelMapper channelMapper,
      MessageService messageService) {
    this.towerRepository = towerRepository;
    this.channelRepository = channelRepository;
    this.channelMapper = channelMapper;
    this.messageService = messageService;
  }

  @Transactional
  public ChannelDto createChannel(String towerId, ChannelDto channelDto) {
    if (!towerRepository.existsById(towerId)) { // TODO: Make a proper exception
      throw new RuntimeException("Tower not found");
    }
    Channel newChannel = channelMapper.channelDtoToChannel(channelDto);
    newChannel.setTowerId(towerId);
    newChannel = channelRepository.save(newChannel);
    return channelMapper.channelToChannelDto(newChannel);
  }

  public List<ChannelDto> getChannels() {
    return Streams.stream(channelRepository.findAll())
        .map(channelMapper::channelToChannelDto)
        .collect(Collectors.toList());
  }

  @Transactional
  public void deleteChannel(String towerId, String channelId) {
    if (!towerRepository.existsById(towerId)) {
      // TODO: Make a proper exception
      throw new RuntimeException("Tower not found");
    }
    Optional<Channel> targetChannel = channelRepository.findById(channelId);
    if (targetChannel.isEmpty()) {
      throw new RuntimeException("Channel not found");
    }
    if (!targetChannel.get().getTowerId().equals(towerId)) {
      throw new RuntimeException("Channel exists but is not part of tower with ID " + towerId);
    }
    channelRepository.deleteById(channelId);
    // Delete all messages that were in chis channel.
    messageService.deleteAllByChannelId(channelId);
  }
}
