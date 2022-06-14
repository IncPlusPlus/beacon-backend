package io.github.incplusplus.beacon.centralidentityserver.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.incplusplus.beacon.centralidentityserver.exception.UserAlreadyExistsException;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.CreateAccountRequestDto;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.UserAccountDto;
import io.github.incplusplus.beacon.centralidentityserver.mapper.UserMapper;
import io.github.incplusplus.beacon.centralidentityserver.persistence.dao.UserRepository;
import io.github.incplusplus.beacon.centralidentityserver.persistence.model.User;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@DataMongoTest
@SpringJUnitConfig
class UserServiceTest extends AbstractServiceTest {
  @Autowired UserMapper userMapper;

  @Autowired UserRepository userRepository;

  private UserService userService;

  @BeforeEach
  void setupTest() {
    userService = new UserService(userRepository, encoder, userMapper, "key", "secret");
  }

  @AfterEach
  void cleanupTest() {
    userRepository.deleteAll();
  }

  public User user1 =
      new User(
          "620550e214e31b50d95f26ff",
          "sampletext@comcast.net",
          "testAccount1",
          "superSecretPassword1",
          "");
  public User user2 =
      new User(
          "j5lkjklkljlkjkljlkj4lk43",
          "bruh@google.com",
          "testAccount2",
          "superSecretPassword2",
          "");
  public User user3 =
      new User(
          "n4b3k5n5i35k63mfiv8vy3b9", "null@void.help", "testAccount3", "superSecretPassword3", "");

  @Test
  void registerNewUserAccount() {
    UserAccountDto newAccountDto =
        userService.registerNewUserAccount(
            new CreateAccountRequestDto()
                .username(user1.getUsername())
                .emailAddress(user1.getEmailAddress())
                .password(user1.getPassword()));
    assertThat(newAccountDto.getId()).isNotEmpty();
    User newAccount =
        new User(
            newAccountDto.getId(),
            user1.getEmailAddress(),
            user1.getUsername(),
            encoder.encode(user1.getPassword()),
            "");
    assertThat(userService.getAccountById(newAccount.getId()))
        .get()
        .usingRecursiveComparison()
        .ignoringFields("password")
        .isEqualTo(newAccount);
    assertThat(encoder.matches(user1.getPassword(), newAccount.getPassword())).isTrue();
  }

  @Test
  void registerNewUserAccountWithExistingEmail() {
    UserAccountDto newAccountDto1 =
        userService.registerNewUserAccount(
            new CreateAccountRequestDto()
                .username(user1.getUsername())
                .emailAddress(user1.getEmailAddress())
                .password(user1.getPassword()));
    assertThatThrownBy(
            () ->
                userService.registerNewUserAccount(
                    new CreateAccountRequestDto()
                        .username(user2.getUsername())
                        .emailAddress(user1.getEmailAddress())
                        .password(user2.getPassword())))
        .isInstanceOf(UserAlreadyExistsException.class)
        .hasMessageContaining("email");
  }

  @Test
  void registerNewUserAccountWithExistingUsername() {
    UserAccountDto newAccountDto1 =
        userService.registerNewUserAccount(
            new CreateAccountRequestDto()
                .username(user1.getUsername())
                .emailAddress(user1.getEmailAddress())
                .password(user1.getPassword()));
    assertThatThrownBy(
            () ->
                userService.registerNewUserAccount(
                    new CreateAccountRequestDto()
                        .username(user1.getUsername())
                        .emailAddress(user2.getEmailAddress())
                        .password(user2.getPassword())))
        .isInstanceOf(UserAlreadyExistsException.class)
        .hasMessageContaining("username");
  }

  @Test
  void ensureEmailRedactedIfAppropriate() {
    UserAccountDto newAccountDto1 =
        userService.registerNewUserAccount(
            new CreateAccountRequestDto()
                .username(user1.getUsername())
                .emailAddress(user1.getEmailAddress())
                .password(user1.getPassword()));
    UserAccountDto newAccountDto2 =
        userService.registerNewUserAccount(
            new CreateAccountRequestDto()
                .username(user2.getUsername())
                .emailAddress(user2.getEmailAddress())
                .password(user2.getPassword()));
    Optional<UserAccountDto> sameAccountRequest =
        userService.publicGetAccountById(newAccountDto1.getId(), newAccountDto1.getUsername());
    Optional<UserAccountDto> differentAccountRequest =
        userService.publicGetAccountById(newAccountDto1.getId(), newAccountDto2.getUsername());
    assertThat(sameAccountRequest).isPresent();
    assertThat(differentAccountRequest).isPresent();
    assertThat(sameAccountRequest.get().getEmailAddress()).isNotEmpty();
    assertThat(differentAccountRequest.get().getEmailAddress()).isEmpty();
  }

  @Test
  void testRetrievingAccount() {
    userService.registerNewUserAccount(
        new CreateAccountRequestDto()
            .username(user1.getUsername())
            .emailAddress(user1.getEmailAddress())
            .password(user1.getPassword()));
    Optional<User> byUsername = userService.getAccountByUsername(user1.getUsername());
    assertThat(byUsername).isPresent();
    assertThat(byUsername)
        .get()
        .usingRecursiveComparison()
        .ignoringFields("password", "id")
        .isEqualTo(user1);
    assertThat(encoder.matches(user1.getPassword(), byUsername.get().getPassword())).isTrue();
  }
}
