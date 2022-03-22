package io.github.incplusplus.beacon.city.service;

import com.google.common.collect.Streams;
import io.github.incplusplus.beacon.city.generated.dto.TowerDto;
import io.github.incplusplus.beacon.city.mapper.TowerMapper;
import io.github.incplusplus.beacon.city.persistence.dao.TowerRepository;
import io.github.incplusplus.beacon.city.persistence.model.Tower;
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

  public TowerService(
      @Autowired TowerMapper towerMapper,
      @Autowired TowerRepository towerRepository,
      AutoRegisterCity autoRegisterCity) {
    this.towerMapper = towerMapper;
    this.towerRepository = towerRepository;
    this.autoRegisterCity = autoRegisterCity;
  }

  public TowerDto createTower(TowerDto towerDto) {
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
}
