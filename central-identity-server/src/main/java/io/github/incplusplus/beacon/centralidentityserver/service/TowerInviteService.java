package io.github.incplusplus.beacon.centralidentityserver.service;

import io.github.incplusplus.beacon.centralidentityserver.generated.dto.TowerInviteDto;
import io.github.incplusplus.beacon.centralidentityserver.mapper.TowerInviteMapper;
import io.github.incplusplus.beacon.centralidentityserver.persistence.dao.TowerInviteRepository;
import io.github.incplusplus.beacon.centralidentityserver.persistence.model.TowerInvite;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TowerInviteService {
  private final TowerInviteRepository inviteRepository;
  private final TowerInviteMapper inviteMapper;

  @Autowired
  public TowerInviteService(
      TowerInviteRepository inviteRepository, TowerInviteMapper inviteMapper) {
    this.inviteRepository = inviteRepository;
    this.inviteMapper = inviteMapper;
  }

  public Optional<TowerInviteDto> getInvite(String towerInviteCode) {
    return inviteRepository
        .findByInviteCode(towerInviteCode)
        .map(inviteMapper::towerInviteToTowerInviteDto);
  }

  public List<TowerInviteDto> getInvitesForCityAndTower(String cityId, String towerId) {
    return inviteRepository.findAllByCityIdAndTowerId(cityId, towerId).stream()
        .map(inviteMapper::towerInviteToTowerInviteDto)
        .toList();
  }

  public Optional<TowerInviteDto> incrementInviteUsesIfAllowed(String towerInviteCode) {
    Optional<TowerInvite> inviteOptional = inviteRepository.findByInviteCode(towerInviteCode);
    TowerInvite invite;
    if (inviteOptional.isPresent()) {
      invite = inviteOptional.get();
      assertInviteUsable(invite);
      // Increment the number of times this invite has been used by 1
      invite.setUses(invite.getUses() + 1);
      invite = inviteRepository.save(invite);
      return Optional.ofNullable(inviteMapper.towerInviteToTowerInviteDto(invite));
    }
    // This will just return the empty optional which will cause a 404 (desired behavior)
    return inviteOptional.map(inviteMapper::towerInviteToTowerInviteDto);
  }

  private void assertInviteUsable(TowerInvite invite) {
    if (invite.isRevoked()) {
      // TODO: Throw an exception that will inform problem-spring-web that this should be a 400 err
      throw new RuntimeException("Invite is revoked");
    }
    // If there's an expiry time, check if we have passed it
    if (invite.getExpiryDate() != null && Instant.now().isAfter(invite.getExpiryDate())) {
      // TODO: Throw an exception that will inform problem-spring-web that this should be a 400 err
      throw new RuntimeException("Invite has expired");
    }
    // If maxUses is 0, infinitely many uses are allowed. Otherwise, maxUses specifies the use limit
    if (invite.getMaxUses() > 0 && invite.getUses() >= invite.getMaxUses()) {
      // TODO: Throw an exception that will inform problem-spring-web that this should be a 400 err
      throw new RuntimeException("Invite has reached the limit for number of uses");
    }
  }

  public Optional<TowerInviteDto> revokeInvite(String towerInviteCode) {
    Optional<TowerInvite> inviteOptional = inviteRepository.findByInviteCode(towerInviteCode);
    TowerInvite invite;
    if (inviteOptional.isPresent()) {
      invite = inviteOptional.get();
      invite.setRevoked(true);
      invite = inviteRepository.save(invite);
      return Optional.ofNullable(inviteMapper.towerInviteToTowerInviteDto(invite));
    }
    return inviteOptional.map(inviteMapper::towerInviteToTowerInviteDto);
  }

  @Transactional
  public TowerInviteDto createInvite(String cityId, TowerInviteDto towerInviteDto) {
    TowerInvite invite = inviteMapper.towerInviteDtoToTowerInvite(towerInviteDto);
    // Set all fields which should be determined by us, not the City that sent it
    invite.setInviteCode(generateUnusedInviteCode());
    invite.setCityId(cityId);
    invite.setDateCreated(Instant.now());
    invite.setUses(0);
    invite.setRevoked(false);

    invite = inviteRepository.save(invite);
    return inviteMapper.towerInviteToTowerInviteDto(invite);
  }

  /**
   * Tries to generate a random eight character alphanumeric string that is unique among the
   * TowerInvites in the database.
   *
   * @return a random eight character alphanumeric string
   */
  private String generateUnusedInviteCode() {
    boolean uniqueInviteCodeFound = false;
    int stringLength = 8;
    String AlphaNumericString =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "abcdefghijklmnopqrstuvwxyz";
    String inviteCode = null;

    while (!uniqueInviteCodeFound) {
      // Adapted from method 1 of
      // https://www.geeksforgeeks.org/generate-random-string-of-given-size-in-java/
      StringBuilder sb = new StringBuilder(stringLength);
      for (int i = 0; i < stringLength; i++) {
        // generate a random number between
        // 0 to AlphaNumericString variable length
        int index = (int) (AlphaNumericString.length() * Math.random());
        sb.append(AlphaNumericString.charAt(index));
      }
      inviteCode = sb.toString();
      if (!inviteRepository.existsById(inviteCode)) {
        uniqueInviteCodeFound = true;
      }
    }
    return inviteCode;
  }
}
