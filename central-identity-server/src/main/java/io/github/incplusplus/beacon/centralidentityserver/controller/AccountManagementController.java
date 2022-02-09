package io.github.incplusplus.beacon.centralidentityserver.controller;

import io.github.incplusplus.beacon.centralidentityserver.generated.controller.AccountManagementApi;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.CreateAccountRequestDto;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.UserAccountDto;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountManagementController implements AccountManagementApi {

  @Override
  public ResponseEntity<UserAccountDto> createNewAccount(
      CreateAccountRequestDto createAccountRequestDto) {
    return null;
  }

  @Override
  public ResponseEntity<UserAccountDto> getAccount(String userAccountId) {
    return null;
  }

  @Override
  public ResponseEntity<UserAccountDto> updateProfilePicture(Resource body) {
    return null;
  }
}
