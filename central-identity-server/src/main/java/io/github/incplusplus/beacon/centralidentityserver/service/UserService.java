package io.github.incplusplus.beacon.centralidentityserver.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import io.github.incplusplus.beacon.centralidentityserver.exception.UserAlreadyExistsException;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.CreateAccountRequestDto;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.UserAccountDto;
import io.github.incplusplus.beacon.centralidentityserver.mapper.UserMapper;
import io.github.incplusplus.beacon.centralidentityserver.persistence.dao.UserRepository;
import io.github.incplusplus.beacon.centralidentityserver.persistence.model.User;
import io.github.incplusplus.beacon.common.exception.StorageException;
import java.io.IOException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class UserService {

  private final AmazonS3 s3Client;

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final UserMapper userMapper;

  @Autowired
  public UserService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      UserMapper userMapper,
      @Value("${do.spaces.key:invalidKey}") String digitalOceanSpacesKey,
      @Value("${do.spaces.secret:invalidSecret}") String digitalOceanSpacesSecret) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.userMapper = userMapper;
    BasicAWSCredentials creds =
        new BasicAWSCredentials(digitalOceanSpacesKey, digitalOceanSpacesSecret);
    // I know I have hardcoded values here.
    // These won't change since we're hosting this, so it doesn't matter.
    this.s3Client =
        AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(
                new EndpointConfiguration("nyc3.digitaloceanspaces.com", "nyc3"))
            .withCredentials(new AWSStaticCredentialsProvider(creds))
            .build();
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

    // Set blank profile picture URL
    user.setProfilePictureUrl("");
    user.setPassword(passwordEncoder.encode(createAccountRequestDto.getPassword()));
    return userMapper.userToUserDto(userRepository.save(user));
  }

  private boolean emailExists(final String email) {
    return userRepository.findByEmailAddress(email).isPresent();
  }

  private boolean usernameExists(final String username) {
    return userRepository.findByUsername(username).isPresent();
  }

  /**
   * @param userAccountId the ID of the account to find. If blank, this will request info about the
   *     account that sent this request (retrieved from the second parameter)
   * @param usernameOfAccountRequestingInfo the ID of the account requesting this information
   * @return the account information requested. For privacy reasons, the email address will be
   *     redacted if the requested account is not the same as the account sending the request.
   */
  public Optional<UserAccountDto> publicGetAccountById(
      String userAccountId, String usernameOfAccountRequestingInfo) {
    Optional<User> accountInQuestion;
    // If no user account ID was provided
    if (userAccountId == null || userAccountId.isBlank()) {
      // Then the request is actually asking for info about the user who sent the request
      accountInQuestion = userRepository.findByUsername(usernameOfAccountRequestingInfo);
    } else {
      // Otherwise, find the account denoted by the ID that was provided in the request
      accountInQuestion = userRepository.findById(userAccountId);
    }
    return accountInQuestion
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

  public Optional<User> getAccountById(String id) {
    return userRepository.findById(id);
  }

  public Optional<User> getAccountByUsername(String username) {
    return userRepository.findByUsername(username);
  }

  public Optional<UserAccountDto> store(MultipartFile picture, String userId) {
    String fileKey = "pfps/" + userId + ".png";
    ObjectMetadata metadata = new ObjectMetadata();
    try {
      metadata.setContentLength(picture.getInputStream().available());
      if (picture.getContentType() != null && !"".equals(picture.getContentType())) {
        metadata.setContentType(picture.getContentType());
      }
      s3Client.putObject(
          new PutObjectRequest("beaconcdn", fileKey, picture.getInputStream(), metadata)
              .withCannedAcl(CannedAccessControlList.PublicRead));
    } catch (IOException e) {
      throw new StorageException("Failed to store file.", e);
    }

    // Get the user object from the database
    Optional<User> accountOptional = userRepository.findById(userId);
    // Somehow the account might not exist. We'll do this just in case
    if (accountOptional.isPresent()) {
      User user = accountOptional.get();
      // Update the profile picture URL on their account
      user.setProfilePictureUrl(getProfilePictureEdgeUrlForId(userId));
      // Save the changes, and put it back in the Optional which will then be converted to a DTO
      accountOptional = Optional.of(userRepository.save(user));
    }
    return accountOptional.map(userMapper::userToUserDto);
  }

  private String getProfilePictureUrlForId(String userId) {
    return "https://beaconcdn.nyc3.digitaloceanspaces.com/pfps/" + userId + ".png";
  }

  private String getProfilePictureEdgeUrlForId(String userId) {
    return "https://beaconcdn.nyc3.cdn.digitaloceanspaces.com/pfps/" + userId + ".png";
  }
}
