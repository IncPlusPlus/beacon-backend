package io.github.incplusplus.beacon.centralidentityserver.controller;

import io.github.incplusplus.beacon.centralidentityserver.exception.UnsupportedFileTypeException;
import io.github.incplusplus.beacon.centralidentityserver.generated.controller.AccountManagementApi;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.CreateAccountRequestDto;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.UserAccountDto;
import io.github.incplusplus.beacon.centralidentityserver.security.IAuthenticationFacade;
import io.github.incplusplus.beacon.centralidentityserver.service.StorageService;
import io.github.incplusplus.beacon.centralidentityserver.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
public class AccountManagementController implements AccountManagementApi {

  private final UserService userService;
  private final IAuthenticationFacade authenticationFacade;
  private final StorageService storageService;

  @Autowired
  public AccountManagementController(
      UserService userService,
      IAuthenticationFacade authenticationFacade,
      StorageService storageService) {
    this.userService = userService;
    this.authenticationFacade = authenticationFacade;
    this.storageService = storageService;
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
    return new ResponseEntity<>(registered, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<UserAccountDto> getAccount(String userAccountId) {
    return ResponseEntity.of(
        userService.publicGetAccountById(
            userAccountId, authenticationFacade.getAuthentication().getName()));
  }

  @Override
  public ResponseEntity<Resource> getProfilePicture(String userAccountID) {
    return ResponseEntity.ok(storageService.loadAsResource(userAccountID + ".png"));
  }

  @Override
  public ResponseEntity<UserAccountDto> updateProfilePicture(MultipartFile picture) {
    if (picture.getContentType() == null || !picture.getContentType().equals("image/png")) {
      throw new UnsupportedFileTypeException(picture.getContentType(), "image/png");
    }
    storageService.store(
        picture,
        userService
            .getAccountByUsername(authenticationFacade.getAuthentication().getName())
            .orElseThrow()
            .getId());
    return null;
  }
}
