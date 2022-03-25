package io.github.incplusplus.beacon.city.service;

import com.google.common.collect.Streams;
import io.github.incplusplus.beacon.city.generated.dto.TowerDto;
import io.github.incplusplus.beacon.city.mapper.TowerMapper;
import io.github.incplusplus.beacon.city.persistence.dao.TowerRepository;
import io.github.incplusplus.beacon.city.persistence.model.Tower;
import io.github.incplusplus.beacon.city.security.LoginAuthenticationProvider;
import io.github.incplusplus.beacon.city.spring.AutoRegisterCity;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TowerService {
  private static final String CIS_MEMBERSHIPS_ENDPOINT_URI = "/city-cis-intercom/memberships";
  private final TowerMapper towerMapper;
  private final TowerRepository towerRepository;
  private final AutoRegisterCity autoRegisterCity;
  private final LoginAuthenticationProvider loginAuthenticationProvider;
  private WebClient cisWebClient;

  @Autowired
  public TowerService(
      TowerMapper towerMapper,
      TowerRepository towerRepository,
      AutoRegisterCity autoRegisterCity,
      LoginAuthenticationProvider loginAuthenticationProvider) {
    this.towerMapper = towerMapper;
    this.towerRepository = towerRepository;
    this.autoRegisterCity = autoRegisterCity;
    this.loginAuthenticationProvider = loginAuthenticationProvider;
  }

  public TowerDto createTower(String username, TowerDto towerDto) {
    String userId = loginAuthenticationProvider.getIdForUsername(username);
    // Initialize member IDs
    towerDto.setMemberAccountIds(new ArrayList<>());
    // Initialize moderator ID list
    towerDto.setModeratorAccountIds(new ArrayList<>());
    // Save this new Tower to the database
    Tower tower = towerRepository.save(towerMapper.towerDtoToTower(towerDto));
    // Make the creator of the tower join the tower and return the changed tower
    return joinTower(username, tower.getId());
  }

  public List<TowerDto> getTowers() {
    return Streams.stream(towerRepository.findAll())
        .map(tower -> towerMapper.towerToTowerDto(tower, autoRegisterCity.getCityId()))
        .collect(Collectors.toList());
  }

  public Optional<TowerDto> getTower(String towerId) {
    Optional<TowerDto> towerDto =
        towerRepository
            .findById(towerId)
            .map(tower -> towerMapper.towerToTowerDto(tower, autoRegisterCity.getCityId()));
    towerDto.ifPresent(dto -> dto.setCityId(autoRegisterCity.getCityId()));
    return towerDto;
  }

  /**
   * Get the list of Towers the specified user is a member of
   *
   * @param usernameOrAccountId the username or account ID of the target user
   * @param isUsername true if {@code usernameOrAccountId} is being provided a username, false if it
   *     is a user ID
   * @return a list of Towers that the specified user is a member of
   */
  public List<TowerDto> getTowersUserIsMemberOf(String usernameOrAccountId, boolean isUsername) {
    String userAccountId =
        isUsername
            ? loginAuthenticationProvider.getIdForUsername(usernameOrAccountId)
            : usernameOrAccountId;
    return towerRepository.findAllByMemberAccountIdsContains(userAccountId).stream()
        .map(tower -> towerMapper.towerToTowerDto(tower, autoRegisterCity.getCityId()))
        .collect(Collectors.toList());
  }

  public boolean isUserMemberOfTower(String username, String towerId) {
    String userId = loginAuthenticationProvider.getIdForUsername(username);
    Optional<Tower> towerOptional = towerRepository.findById(towerId);
    if (towerOptional.isEmpty()) {
      // TODO: Make a proper exception
      throw new RuntimeException("Tower not found");
    }
    return towerOptional.get().getMemberAccountIds().contains(userId);
  }

  public TowerDto joinTower(String username, String towerId) {
    String userId = loginAuthenticationProvider.getIdForUsername(username);
    Optional<Tower> towerOptional = towerRepository.findById(towerId);
    if (towerOptional.isEmpty()) {
      // TODO: Make a proper exception
      throw new RuntimeException("Tower not found");
    }
    Tower tower = towerOptional.get();
    if (tower.getMemberAccountIds().contains(userId)) {
      // TODO: Make a proper exception
      throw new RuntimeException("User is already a member of that tower");
    }
    // region Contact the CIS to add a new "member of this City" if applicable
    boolean userPartOfCityBeforeJoiningThisTower =
        towerRepository.existsTowerByMemberAccountIdsContains(userId);
    // If this is the first Tower hosted by this City that the user has joined...
    if (!userPartOfCityBeforeJoiningThisTower) {
      // Let the CIS know that the user is now "a member of this City"
      try {
        List<String> membersOfCity =
            getCisWebClient()
                .put()
                //            .contentType(MediaType.APPLICATION_JSON)
                .uri(CIS_MEMBERSHIPS_ENDPOINT_URI)
                .bodyValue(List.of(userId))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                .block(Duration.ofSeconds(30));
      } catch (Exception e) {
        // TODO: Make a proper exception that reflects that the City-to-CIS communication failed. It
        //  should cause a 500 error as this is an internal issue.
        throw new RuntimeException(e);
      }
    }
    // endregion
    tower.getMemberAccountIds().add(userId);
    return towerMapper.towerToTowerDto(towerRepository.save(tower), autoRegisterCity.getCityId());
  }

  public TowerDto leaveTower(String username, String towerId) {
    String userId = loginAuthenticationProvider.getIdForUsername(username);
    Optional<Tower> towerOptional = towerRepository.findById(towerId);
    if (towerOptional.isEmpty()) {
      // TODO: Make a proper exception
      throw new RuntimeException("Tower not found");
    }
    Tower tower = towerOptional.get();
    if (!tower.getMemberAccountIds().contains(userId)) {
      // TODO: Make a proper exception
      throw new RuntimeException(
          "User is no longer in that tower. Can't leave a tower you weren't even in.");
    }
    // region Contact the CIS to remove the "member of this City" if applicable
    boolean userWillNoLongerBeMemberOfAnyTowersWithinThisCity =
        towerRepository.countTowersByMemberAccountIdsContains(userId) < 2;
    // If the user will no longer be a member of any Towers within this City
    // after they're removed from this tower...
    if (userWillNoLongerBeMemberOfAnyTowersWithinThisCity) {
      // Let the CIS know to remove them from the "members of this City"
      try {
        List<String> membersOfCity =
            getCisWebClient()
                .method(HttpMethod.DELETE)
                //            .contentType(MediaType.APPLICATION_JSON)
                .uri(CIS_MEMBERSHIPS_ENDPOINT_URI)
                .bodyValue(List.of(userId))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                .block(Duration.ofSeconds(30));
      } catch (Exception e) {
        // TODO: Make a proper exception that reflects that the City-to-CIS communication failed. It
        // should cause a 500 error as this is an internal issue.
        throw new RuntimeException(e);
      }
    }
    // endregion
    tower.getMemberAccountIds().remove(userId);
    return towerMapper.towerToTowerDto(towerRepository.save(tower), autoRegisterCity.getCityId());
  }

  public void deleteTower(String towerId) {
    if (towerRepository.existsById(towerId)) {
      // region Contact the CIS to remove "members of this City" if applicable
      // Get the list of members in this specific Tower
      @SuppressWarnings("OptionalGetWithoutIsPresent")
      List<String> membersOfThisTower =
          towerRepository.findById(towerId).get().getMemberAccountIds();
      // Get the list of members of all towers except this one
      List<String> membersOfOtherTowers =
          towerRepository.findTowersByIdIsNot(towerId).stream()
              // Create a stream of all the members of all the selected Towers
              .flatMap(tower -> tower.getMemberAccountIds().stream())
              // Remove duplicates
              .distinct()
              .toList();
      // Get the list of members who are unique to this Tower and aren't members of any other
      // Tower within this City.
      List<String> membersUniqueToThisTower =
          membersOfOtherTowers.stream()
              .filter(member -> !membersOfOtherTowers.contains(member))
              .toList();

      // Let the CIS know to remove the "members of this City" who are members of this Tower and no
      // other Towers within this City.
      List<String> membersOfCity =
          getCisWebClient()
              .method(HttpMethod.DELETE)
              //            .contentType(MediaType.APPLICATION_JSON)
              .uri(CIS_MEMBERSHIPS_ENDPOINT_URI)
              .bodyValue(membersUniqueToThisTower)
              .retrieve()
              .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
              .block(Duration.ofSeconds(30));
      // endregion
      towerRepository.deleteById(towerId);
      // TODO: Delete the channels in this Tower (and the messages too)
    } else {
      // TODO: Make this a proper exception
      throw new RuntimeException("Tower not found");
    }
  }

  /**
   * @return the WebClient for interacting with the CIS. This isn't put into a field in the
   *     constructor because the City ID and password aren't initialized at start and the
   *     setBasicAuth method will throw an exception if either of its parameters are null.
   */
  private WebClient getCisWebClient() {
    if (this.cisWebClient == null) {
      this.cisWebClient =
          WebClient.builder()
              .baseUrl(autoRegisterCity.getCISUrl())
              .defaultHeaders(
                  header ->
                      header.setBasicAuth(
                          autoRegisterCity.getCityId(), autoRegisterCity.getCityCISPassword()))
              .build();
    }
    return this.cisWebClient;
  }
}
