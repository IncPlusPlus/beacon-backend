package io.github.incplusplus.beacon.city.controller;

import io.github.incplusplus.beacon.city.generated.controller.ChannelsApi;
import io.github.incplusplus.beacon.city.generated.dto.ChannelDto;
import io.github.incplusplus.beacon.city.service.ChannelService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ChannelController implements ChannelsApi {
  private final ChannelService channelService;

  public ChannelController(@Autowired ChannelService channelService) {
    this.channelService = channelService;
  }

  @Override
  public ResponseEntity<ChannelDto> createChannel(String towerId, ChannelDto channelDto) {
    return ResponseEntity.ok(channelService.createChannel(towerId, channelDto));
  }

  @Override
  public ResponseEntity<ChannelDto> deleteChannel(String towerId, String channelId) {
    return ResponseEntity.of(channelService.deleteChannel(towerId, channelId));
  }

  @Override
  public ResponseEntity<ChannelDto> editChannel(
      String towerId, String channelId, ChannelDto channelDto) {
    return ResponseEntity.ok(channelService.editChannel(towerId, channelId, channelDto));
  }

  @Override
  public ResponseEntity<List<ChannelDto>> getChannels(String towerId) {
    return ResponseEntity.ok(channelService.getChannels());
  }
}
