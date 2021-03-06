package io.github.incplusplus.beacon.city.service;

import com.google.common.collect.Streams;
import io.github.incplusplus.beacon.city.exception.CisCommunicationsException;
import io.github.incplusplus.beacon.city.exception.InvalidRequestException;
import io.github.incplusplus.beacon.city.generated.dto.TowerDto;
import io.github.incplusplus.beacon.city.mapper.TowerMapper;
import io.github.incplusplus.beacon.city.persistence.dao.TowerRepository;
import io.github.incplusplus.beacon.city.persistence.model.Tower;
import io.github.incplusplus.beacon.city.security.LoginAuthenticationProvider;
import io.github.incplusplus.beacon.city.spring.AutoRegisterCity;
import io.github.incplusplus.beacon.city.websocket.notifier.TowerNotifier;
import io.github.incplusplus.beacon.common.exception.EntityDoesNotExistException;
import io.github.incplusplus.beacon.common.exception.StorageException;
import io.github.incplusplus.beacon.common.exception.UnsupportedFileTypeException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TowerService {
  private final TowerMapper towerMapper;
  private final TowerRepository towerRepository;
  private final AutoRegisterCity autoRegisterCity;
  private final CisCommunicationsService cisCommunicationsService;
  private final LoginAuthenticationProvider loginAuthenticationProvider;
  private final TowerNotifier towerNotifier;
  private final StorageService storageService;

  @Autowired
  public TowerService(
      TowerMapper towerMapper,
      TowerRepository towerRepository,
      AutoRegisterCity autoRegisterCity,
      CisCommunicationsService cisCommunicationsService,
      LoginAuthenticationProvider loginAuthenticationProvider,
      TowerNotifier towerNotifier,
      StorageService storageService) {
    this.towerMapper = towerMapper;
    this.towerRepository = towerRepository;
    this.autoRegisterCity = autoRegisterCity;
    this.cisCommunicationsService = cisCommunicationsService;
    this.loginAuthenticationProvider = loginAuthenticationProvider;
    this.towerNotifier = towerNotifier;
    this.storageService = storageService;
  }

  public TowerDto createTower(String username, TowerDto towerDto) {
    String userId = loginAuthenticationProvider.getIdForUsername(username);
    // Set the Tower creator as the admin of this Tower
    towerDto.setAdminAccountId(userId);
    // Initialize member IDs
    towerDto.setMemberAccountIds(new ArrayList<>());
    // Initialize moderator ID list
    towerDto.setModeratorAccountIds(new ArrayList<>());
    // Set default primary and secondary colors
    towerDto.setPrimaryColor("FFD800");
    towerDto.setSecondaryColor("5e5d59");
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

  public TowerDto getTower(String towerId) {
    return towerRepository
        .findById(towerId)
        .map(tower -> towerMapper.towerToTowerDto(tower, autoRegisterCity.getCityId()))
        .orElseThrow(() -> new EntityDoesNotExistException("Tower", towerId));
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
    Tower tower =
        towerRepository
            .findById(towerId)
            .orElseThrow(() -> new EntityDoesNotExistException("Tower", towerId));
    return tower.getMemberAccountIds().contains(userId);
  }

  public TowerDto joinTower(String username, String towerId) {
    String userId = loginAuthenticationProvider.getIdForUsername(username);
    Tower tower =
        towerRepository
            .findById(towerId)
            .orElseThrow(() -> new EntityDoesNotExistException("Tower", towerId));
    if (tower.getMemberAccountIds().contains(userId)) {
      throw new InvalidRequestException(
          "User '%s' (ID: %s) is already a member of Tower '%s' (ID: %s)."
              .formatted(username, userId, tower.getName(), towerId));
    }
    // region Contact the CIS to add a new "member of this City" if applicable
    boolean userPartOfCityBeforeJoiningThisTower =
        towerRepository.existsTowerByMemberAccountIdsContains(userId);
    // If this is the first Tower hosted by this City that the user has joined...
    if (!userPartOfCityBeforeJoiningThisTower) {
      // Let the CIS know that the user is now "a member of this City"
      try {
        List<String> membersOfCity = cisCommunicationsService.addCityMembers(userId);
      } catch (Exception e) {
        throw new CisCommunicationsException(
            "trying to let the CIS know that the current user is now a member of a Tower within this City",
            e);
      }
    }
    // endregion
    tower.getMemberAccountIds().add(userId);
    return towerMapper.towerToTowerDto(towerRepository.save(tower), autoRegisterCity.getCityId());
  }

  public TowerDto leaveTower(String username, String towerId) {
    String userId = loginAuthenticationProvider.getIdForUsername(username);
    Tower tower =
        towerRepository
            .findById(towerId)
            .orElseThrow(() -> new EntityDoesNotExistException("Tower", towerId));
    if (!tower.getMemberAccountIds().contains(userId)) {
      throw new InvalidRequestException(
          "User is no longer in that Tower. Can't leave a Tower you weren't even in.");
    }
    // region Contact the CIS to remove the "member of this City" if applicable
    boolean userWillNoLongerBeMemberOfAnyTowersWithinThisCity =
        towerRepository.countTowersByMemberAccountIdsContains(userId) < 2;
    // If the user will no longer be a member of any Towers within this City
    // after they're removed from this tower...
    if (userWillNoLongerBeMemberOfAnyTowersWithinThisCity) {
      // Let the CIS know to remove them from the "members of this City"
      try {
        List<String> membersOfCity = cisCommunicationsService.removeCityMembers(List.of(userId));
      } catch (Exception e) {
        throw new CisCommunicationsException(
            "trying to let the CIS know that the current user is no longer a member of any Towers within this City",
            e);
      }
    }
    // endregion
    tower.getMemberAccountIds().remove(userId);
    return towerMapper.towerToTowerDto(towerRepository.save(tower), autoRegisterCity.getCityId());
  }

  public void deleteTower(String towerId) {
    Tower tower =
        towerRepository
            .findById(towerId)
            .orElseThrow(() -> new EntityDoesNotExistException("Tower", towerId));
    // region Contact the CIS to remove "members of this City" if applicable
    // Get the list of members in this specific Tower
    List<String> membersOfThisTower = tower.getMemberAccountIds();
    // Get the list of members of all towers except this one
    List<String> membersOfOtherTowers =
        towerRepository.findTowersByIdIsNot(towerId).stream()
            // Create a stream of all the members of all the selected Towers
            .flatMap(thisTower -> thisTower.getMemberAccountIds().stream())
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
        cisCommunicationsService.removeCityMembers(membersUniqueToThisTower);
    // endregion
    // Actually delete this Tower
    towerRepository.deleteById(towerId);
    // TODO: Delete the channels in this Tower (and the messages too)
  }

  public TowerDto editTower(
      String towerId, TowerDto towerDto, MultipartFile icon, MultipartFile banner) {
    if (!towerRepository.existsById(towerId)) {
      throw new EntityDoesNotExistException("Tower", towerId);
    }
    Tower tower =
        towerRepository
            .findById(towerId)
            .orElseThrow(() -> new EntityDoesNotExistException("Tower", towerId));

    try {
      // Update icon and banner depending on what the user uploaded
      tower = updateIcon(tower, icon);
      tower = updateBanner(tower, banner);
    } catch (IOException e) {
      throw new StorageException("Unable to update banner or icon", e);
    }

    // Update the Tower name
    tower.setName(towerDto.getName());
    // Update the Tower admin account id
    tower.setAdminAccountId(towerDto.getAdminAccountId());
    // If it was specified in the request, set the moderators list
    if (towerDto.getModeratorAccountIds() != null) {
      tower.setModeratorAccountIds(towerDto.getModeratorAccountIds());
    }
    // We don't do anything here with the members list because the members list is only modified
    // when a user joins or leaves. We shouldn't be editing it manually.

    // Update the colors. We don't bother checking if they're valid hex values.
    tower.setPrimaryColor(towerDto.getPrimaryColor());
    tower.setSecondaryColor(towerDto.getSecondaryColor());
    // Update the Tower in the database and convert the result to a DTO
    TowerDto editedDto =
        towerMapper.towerToTowerDto(towerRepository.save(tower), autoRegisterCity.getCityId());
    // Notify all subscribed clients that this Tower has been edited
    towerNotifier.notifyEditedTower(editedDto);
    return editedDto;
  }

  private Tower updateIcon(Tower tower, MultipartFile icon) throws IOException {
    if (icon != null) {
      if (icon.getSize() == 0) {
        tower.setIconUrl("");
      } else {
        if (icon.getContentType() == null || !icon.getContentType().equals("image/png")) {
          throw new UnsupportedFileTypeException(icon.getContentType(), "image/png");
        }
        tower.setIconUrl(storageService.saveTowerIcon(icon, tower.getId(), tower.getIconUrl()));
      }
    }
    return tower;
  }

  private Tower updateBanner(Tower tower, MultipartFile banner) throws IOException {
    if (banner != null) {
      if (banner.getSize() == 0) {
        tower.setBannerUrl("");
      } else {
        if (banner.getContentType() == null || !banner.getContentType().equals("image/png")) {
          throw new UnsupportedFileTypeException(banner.getContentType(), "image/png");
        }
        tower.setBannerUrl(
            storageService.saveTowerBanner(banner, tower.getId(), tower.getBannerUrl()));
      }
    }
    return tower;
  }
}
