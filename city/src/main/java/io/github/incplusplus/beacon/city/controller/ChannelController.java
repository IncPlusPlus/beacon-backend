package io.github.incplusplus.beacon.city.controller;

import io.github.incplusplus.beacon.city.generated.controller.ChannelsApi;
import io.github.incplusplus.beacon.city.generated.dto.ChannelDto;
import io.github.incplusplus.beacon.city.service.ChannelService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    return new ResponseEntity<>(
        channelService.createChannel(towerId, channelDto), HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<Void> deleteChannel(String towerId, String channelId) {
    channelService.deleteChannel(towerId, channelId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<List<ChannelDto>> getChannels(String towerId) {
    return ResponseEntity.ok(channelService.getChannels());
  }
}
