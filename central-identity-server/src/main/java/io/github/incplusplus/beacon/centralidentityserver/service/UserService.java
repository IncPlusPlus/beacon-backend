package io.github.incplusplus.beacon.centralidentityserver.service;

import io.github.incplusplus.beacon.centralidentityserver.exception.UserAlreadyExistsException;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.CreateAccountRequestDto;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.UserAccountDto;
import io.github.incplusplus.beacon.centralidentityserver.mapper.UserMapper;
import io.github.incplusplus.beacon.centralidentityserver.persistence.dao.UserRepository;
import io.github.incplusplus.beacon.centralidentityserver.persistence.model.User;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final UserMapper userMapper;

  @Autowired
  public UserService(
      UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.userMapper = userMapper;
  }

  public UserAccountDto registerNewUserAccount(CreateAccountRequestDto createAccountRequestDto) {
    if (emailExists(createAccountRequestDto.getEmailAddress())) {
      throw new UserAlreadyExistsException(
          "email address", createAccountRequestDto.getEmailAddress());
    }
    if (usernameExists(createAccountRequestDto.getUsername())) {
      throw new UserAlreadyExistsException("username", createAccountRequestDto.getUsername());
    }
    final User user = userMapper.createAccountRequestDtoToUser(createAccountRequestDto);

    user.setPassword(passwordEncoder.encode(createAccountRequestDto.getPassword()));
    return userMapper.userToUserDto(userRepository.save(user));
  }

  private boolean emailExists(final String email) {
    return userRepository.findByEmailAddress(email).isPresent();
  }

  private boolean usernameExists(final String username) {
    return userRepository.findByUsername(username).isPresent();
  }

  public Optional<UserAccountDto> getAccountById(
      String userAccountId, String usernameOfAccountRequestingInfo) {
    return userRepository
        .findById(userAccountId)
        // Map user Document to a DTO in preparation for returning to the controller
        .map(userMapper::userToUserDto)
        // Redact the email address if the user who is requesting this info isn't the user who is
        // logged in
        .map(
            userAccountDto -> {
              if (!userAccountDto.getUsername().equals(usernameOfAccountRequestingInfo)) {
                userAccountDto.setEmailAddress("");
              }
              return userAccountDto;
            });
  }

  public Optional<User> getAccountByUsername(String username) {
    return userRepository.findByUsername(username);
  }
}
