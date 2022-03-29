package io.github.incplusplus.beacon.city.controller;

import io.github.incplusplus.beacon.city.generated.controller.MessagesApi;
import io.github.incplusplus.beacon.city.generated.dto.MessageDto;
import io.github.incplusplus.beacon.city.security.IAuthenticationFacade;
import io.github.incplusplus.beacon.city.service.MessageService;
import java.util.List;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class MessageController implements MessagesApi {

  private final MessageService messageService;
  private final IAuthenticationFacade authenticationFacade;

  public MessageController(
      @Autowired MessageService messageService, IAuthenticationFacade authenticationFacade) {
    this.messageService = messageService;
    this.authenticationFacade = authenticationFacade;
  }

  @Override
  public ResponseEntity<MessageDto> createMessage(
      String towerId, String channelId, List<MultipartFile> attachments, MessageDto message) {
    return new ResponseEntity<>(
        messageService.createMessage(
            authenticationFacade.getAuthentication().getName(),
            towerId,
            channelId,
            attachments,
            message),
        HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<MessageDto> deleteMessage(
      String towerId, String channelId, String messageId) {
    // TODO: Implement
    throw new NotImplementedException("Logic to delete messages not implemented yet.");
  }

  @Override
  public ResponseEntity<MessageDto> editMessage(
      String towerId, String channelId, String messageId, MessageDto messageDto) {
    // TODO: Implement
    throw new NotImplementedException("Logic to edit messages not implemented yet.");
  }

  @Override
  public ResponseEntity<List<MessageDto>> getMessages(String towerId, String channelId) {
    return ResponseEntity.ok(messageService.getMessages(towerId, channelId));
  }
}
