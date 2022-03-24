package io.github.incplusplus.beacon.city.service;

import com.google.common.collect.Streams;
import io.github.incplusplus.beacon.city.generated.dto.TowerDto;
import io.github.incplusplus.beacon.city.mapper.TowerMapper;
import io.github.incplusplus.beacon.city.persistence.dao.TowerRepository;
import io.github.incplusplus.beacon.city.persistence.model.Tower;
import io.github.incplusplus.beacon.city.security.LoginAuthenticationProvider;
import io.github.incplusplus.beacon.city.spring.AutoRegisterCity;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TowerService {
  private final TowerMapper towerMapper;
  private final TowerRepository towerRepository;
  private final AutoRegisterCity autoRegisterCity;
  private final LoginAuthenticationProvider loginAuthenticationProvider;

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

  public TowerDto createTower(TowerDto towerDto) {
    // TODO: Add the admin ID to the list of members when the tower is created
    Tower tower = towerRepository.save(towerMapper.towerDtoToTower(towerDto));
    return towerMapper.towerToTowerDto(tower);
  }

  public List<TowerDto> getTowers() {
    return Streams.stream(towerRepository.findAll())
        .map(towerMapper::towerToTowerDto)
        .collect(Collectors.toList());
  }

  public Optional<TowerDto> getTower(String towerId) {
    Optional<TowerDto> towerDto =
        towerRepository.findById(towerId).map(towerMapper::towerToTowerDto);
    towerDto.ifPresent(dto -> dto.setCityId(autoRegisterCity.getCityId()));
    return towerDto;
  }

  public List<TowerDto> getTowersUserIsMemberOf(String userAccountId) {
    return towerRepository.findAllByMemberAccountIdsContains(userAccountId).stream()
        .map(towerMapper::towerToTowerDto)
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

    // TODO: Also contact the CIS to add the "member of this City" if applicable
    tower.getMemberAccountIds().add(userId);
    return towerMapper.towerToTowerDto(towerRepository.save(tower));
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
    // TODO: Also contact the CIS to remove the "member of this City" if applicable
    tower.getMemberAccountIds().remove(userId);
    return towerMapper.towerToTowerDto(towerRepository.save(tower));
  }

  public void deleteTower(String towerId) {
    if (towerRepository.existsById(towerId)) {
      // TODO: Also have this contact the CIS to remove "members of this City" if applicable
      towerRepository.deleteById(towerId);
    } else {
      // TODO: Make this a proper exception
      throw new RuntimeException("Tower not found");
    }
  }
}
