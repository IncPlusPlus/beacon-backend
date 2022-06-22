package io.github.incplusplus.beacon.centralidentityserver.controller;

import io.github.incplusplus.beacon.centralidentityserver.generated.controller.AccountManagementApi;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.CreateAccountRequestDto;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.UserAccountDto;
import io.github.incplusplus.beacon.centralidentityserver.security.IAuthenticationFacade;
import io.github.incplusplus.beacon.centralidentityserver.service.UserService;
import io.github.incplusplus.beacon.common.exception.UnsupportedFileTypeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
public class AccountManagementController implements AccountManagementApi {

  private final UserService userService;
  private final IAuthenticationFacade authenticationFacade;

  @Autowired
  public AccountManagementController(
      UserService userService, IAuthenticationFacade authenticationFacade) {
    this.userService = userService;
    this.authenticationFacade = authenticationFacade;
  }

  @Override
  public ResponseEntity<UserAccountDto> createNewAccount(
      CreateAccountRequestDto createAccountRequestDto) {
    log.debug(
        "Attempting to create new account with email '{}' and username '{}'.",
        createAccountRequestDto.getEmailAddress(),
        createAccountRequestDto.getUsername());
    UserAccountDto registered = userService.registerNewUserAccount(createAccountRequestDto);
    log.debug(
        "Successfully created new account with email '{}' and username '{}'.",
        registered.getEmailAddress(),
        registered.getUsername());
    return ResponseEntity.ok(registered);
  }

  @Override
  public ResponseEntity<UserAccountDto> getAccount(String userAccountId) {
    return ResponseEntity.of(
        userService.publicGetAccountById(
            userAccountId, authenticationFacade.getAuthentication().getName()));
  }

  @Override
  public ResponseEntity<UserAccountDto> updateProfilePicture(MultipartFile picture) {
    if (picture.getContentType() == null || !picture.getContentType().equals("image/png")) {
      throw new UnsupportedFileTypeException(picture.getContentType(), "image/png");
    }
    return ResponseEntity.of(
        userService.store(
            picture,
            userService
                .getAccountByUsername(authenticationFacade.getAuthentication().getName())
                .orElseThrow()
                .getId()));
  }
}
