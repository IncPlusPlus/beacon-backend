package io.github.incplusplus.beacon.city.service;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.incplusplus.beacon.city.generated.dto.TowerDto;
import io.github.incplusplus.beacon.city.mapper.TowerMapper;
import io.github.incplusplus.beacon.city.persistence.dao.TowerRepository;
import io.github.incplusplus.beacon.city.persistence.model.Tower;
import io.github.incplusplus.beacon.city.security.LoginAuthenticationProvider;
import io.github.incplusplus.beacon.city.spring.AutoRegisterCity;
import io.github.incplusplus.beacon.city.websocket.notifier.TowerNotifier;
import io.github.incplusplus.beacon.common.exception.StorageException;
import io.github.incplusplus.beacon.common.exception.UnsupportedFileTypeException;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.multipart.MultipartFile;

@DataMongoTest
@SpringJUnitConfig
public class TowerServiceTest extends AbstractServiceTest {
  @Autowired TowerMapper towerMapper;

  @Autowired TowerRepository towerRepository;

  TowerService towerService;

  @Mock AutoRegisterCity autoRegisterCity;
  @Mock CisCommunicationsService cisCommunicationsService;

  @Mock LoginAuthenticationProvider loginAuthenticationProvider;

  @Mock TowerNotifier towerNotifier;

  @Mock StorageService storageService;

  // region Test data
  private final String cityId = "someCityId";

  // region Users
  String userOneId = "user1Id";
  String userOneName = "UserOne";
  String userTwoId = "user2Id";
  String userTwoName = "UserTwo";
  String userThreeId = "user3Id";
  String userThreeName = "UserThree";
  // endregion

  // region Towers
  Tower t1 = Tower.builder().name("Tower One").adminAccountId(userOneId).build();
  Tower t2 =
      Tower.builder()
          .name("Tower Two")
          .adminAccountId(userTwoId)
          .moderatorAccountIds(asList(userOneId, userThreeId))
          .memberAccountIds(asList(userTwoId, userOneId, userThreeId))
          .iconUrl("https://example.com/cdn/bruh.png")
          .bannerUrl("https://example.com/cdn/bruh_banner.png")
          .build();
  Tower t3 =
      Tower.builder()
          .name("Tower Three")
          .adminAccountId(userThreeId)
          .memberAccountIds(asList(userThreeId, userTwoId))
          .iconUrl("https://foo.bar/icon.png")
          .bannerUrl("https://foo.bar/banner.png")
          .primaryColor("03e3fc")
          .secondaryColor("03e3fc")
          .build();
  // endregion
  MultipartFile t2Icon =
      new MockMultipartFile("bruh", "bruh.png", "image/png", new byte[] {12, 21});
  MultipartFile t2Banner =
      new MockMultipartFile("bruh_banner", "bruh_banner.png", "image/png", new byte[] {21, 12});
  MultipartFile t3Icon =
      new MockMultipartFile("icon", "icon.png", "image/png", new byte[] {23, 32});
  MultipartFile t3Banner =
      new MockMultipartFile("banner", "banner.png", "image/png", new byte[] {32, 23});
  MultipartFile thisUploadShouldThrowIOException =
      new MockMultipartFile("shouldThrow", "shouldThrow.png", "image/png", new byte[] {1});
  // endregion

  @BeforeAll
  static void setUpAll() {}

  @BeforeEach
  void setupTest() throws IOException {
    towerService =
        new TowerService(
            towerMapper,
            towerRepository,
            autoRegisterCity,
            cisCommunicationsService,
            loginAuthenticationProvider,
            towerNotifier,
            storageService);
    // Set up mocks
    when(loginAuthenticationProvider.getIdForUsername(userOneName)).thenReturn(userOneId);
    when(loginAuthenticationProvider.getIdForUsername(userTwoName)).thenReturn(userTwoId);
    when(loginAuthenticationProvider.getIdForUsername(userThreeName)).thenReturn(userThreeId);
    when(autoRegisterCity.getCityId()).thenReturn(cityId);
    when(storageService.saveTowerIcon(eq(t2Icon), anyString(), any()))
        .thenReturn(t2.getIconUrl());
    when(storageService.saveTowerBanner(eq(t2Banner), anyString(), any()))
        .thenReturn(t2.getBannerUrl());
    when(storageService.saveTowerIcon(eq(t3Icon), anyString(), any()))
        .thenReturn(t3.getIconUrl());
    when(storageService.saveTowerBanner(eq(t3Banner), anyString(), any()))
        .thenReturn(t3.getBannerUrl());
    when(storageService.saveTowerIcon(
            eq(thisUploadShouldThrowIOException), anyString(), any()))
        .thenThrow(new IOException("This is meant to be thrown"));
  }

  @AfterEach
  void cleanupTest() {
    towerRepository.deleteAll();
  }

  @Test
  void createTower() {
    TowerDto newTowerApiResponse =
        towerService.createTower(userOneName, towerMapper.towerToTowerDto(t1, cityId));
    assertThat(newTowerApiResponse.getId()).isNotEmpty();

    // Ask about the tower we just made
    TowerDto newTower = towerService.getTower(newTowerApiResponse.getId());
    // Assert that the value returned won't be different the next time we ask about this tower
    assertThat(newTower).usingRecursiveComparison().isEqualTo(newTowerApiResponse);
    assertThat(newTower.getMemberAccountIds()).isNotNull();
    assertThat(newTower.getModeratorAccountIds()).isNotNull();

    // Verify that certain calls happened to make sure the service behaved as expected
    verify(loginAuthenticationProvider, atLeastOnce()).getIdForUsername(userOneName);
  }

  @Test
  void editTowerBasic() throws IOException {
    TowerDto newTowerDto =
        towerService.createTower(userOneName, towerMapper.towerToTowerDto(t2, cityId));

    TowerDto editedTower =
        towerService.editTower(
            newTowerDto.getId(), towerMapper.towerToTowerDto(t2, cityId), null, null);
    assertThat(editedTower)
        .usingRecursiveComparison()
        .ignoringFields("id", "cityId", "memberAccountIds")
        .isEqualTo(t2);

    // The storage service should never be called
    verify(storageService, never()).saveTowerIcon(any(), any(), any());
    verify(storageService, never()).saveTowerBanner(any(), any(), any());
  }

  @Test
  void editTowerAddImages() throws IOException {
    TowerDto newTower =
        towerService.createTower(userOneName, towerMapper.towerToTowerDto(t1, cityId));
    assertThat(newTower.getIconUrl()).isNullOrEmpty();
    assertThat(newTower.getBannerUrl()).isNullOrEmpty();

    TowerDto editedTower = towerService.editTower(newTower.getId(), newTower, t2Icon, t2Banner);
    assertThat(editedTower.getIconUrl()).isEqualTo(t2.getIconUrl());
    assertThat(editedTower.getBannerUrl()).isEqualTo(t2.getBannerUrl());

    // Verify certain methods were called exactly once
    verify(storageService).saveTowerIcon(t2Icon, newTower.getId(),null);
    verify(storageService).saveTowerBanner(t2Banner, newTower.getId(),null);
  }

  @Test
  void editTowerChangeImages() throws IOException {
    TowerDto newTower =
        towerService.createTower(userOneName, towerMapper.towerToTowerDto(t2, cityId));
    assertThat(newTower.getIconUrl()).isEqualTo(t2.getIconUrl());
    assertThat(newTower.getBannerUrl()).isEqualTo(t2.getBannerUrl());

    TowerDto editedTower = towerService.editTower(newTower.getId(), newTower, t3Icon, t3Banner);
    assertThat(editedTower.getIconUrl()).isEqualTo(t3.getIconUrl());
    assertThat(editedTower.getBannerUrl()).isEqualTo(t3.getBannerUrl());

    // Verify certain methods were called exactly once
    verify(storageService).saveTowerIcon(t3Icon, newTower.getId(), t2.getIconUrl());
    verify(storageService).saveTowerBanner(t3Banner, newTower.getId(), t2.getBannerUrl());
  }

  @Test
  void editTowerRemoveImages() throws IOException {
    TowerDto newTower =
        towerService.createTower(userOneName, towerMapper.towerToTowerDto(t3, cityId));
    assertThat(newTower.getIconUrl()).isNotBlank();
    assertThat(newTower.getBannerUrl()).isNotBlank();

    TowerDto iconRemoved =
        towerService.editTower(
            newTower.getId(),
            newTower,
            new MockMultipartFile("file", "file.png", "image/png", new byte[] {}),
            null);
    assertThat(iconRemoved.getIconUrl()).isEmpty();
    assertThat(iconRemoved.getBannerUrl()).isNotBlank();

    TowerDto bannerRemoved =
        towerService.editTower(
            newTower.getId(),
            newTower,
            null,
            new MockMultipartFile("file", "file.png", "image/png", new byte[] {}));
    assertThat(bannerRemoved.getIconUrl()).isEmpty();
    assertThat(bannerRemoved.getBannerUrl()).isEmpty();

    // The storage service should never be called
    verify(storageService, never()).saveTowerIcon(any(), any(), any());
    verify(storageService, never()).saveTowerBanner(any(), any(), any());
  }

  @Test
  void editTowerInvalidImages() throws IOException {
    TowerDto newTower =
        towerService.createTower(userOneName, towerMapper.towerToTowerDto(t1, cityId));

    // The icon Content-Type header here will be null
    assertThatThrownBy(
            () ->
                towerService.editTower(
                    newTower.getId(),
                    newTower,
                    new MockMultipartFile("bruh", new byte[] {9}),
                    t2Banner))
        .isInstanceOf(UnsupportedFileTypeException.class)
        .hasMessageContaining("image/png");
    // The icon Content-Type header here will be application/json
    assertThatThrownBy(
            () ->
                towerService.editTower(
                    newTower.getId(),
                    newTower,
                    new MockMultipartFile("bruh", "bruh.png", "application/json", new byte[] {9}),
                    t2Banner))
        .isInstanceOf(UnsupportedFileTypeException.class)
        .hasMessageContaining("image/png")
        .hasMessageContaining("application/json");
    // The banner Content-Type header here will be null
    assertThatThrownBy(
        () ->
            towerService.editTower(
                newTower.getId(), newTower, t2Icon, new MockMultipartFile("bruh", new byte[] {9})));
    // The banner Content-Type header will be application/json
    assertThatThrownBy(
            () ->
                towerService.editTower(
                    newTower.getId(),
                    newTower,
                    t2Icon,
                    new MockMultipartFile("bruh", "bruh.png", "application/json", new byte[] {9})))
        .isInstanceOf(UnsupportedFileTypeException.class)
        .hasMessageContaining("image/png")
        .hasMessageContaining("application/json");

    // This file is meant to test that we handle an IOException from our StorageService impl
    assertThatThrownBy(
            () ->
                towerService.editTower(
                    newTower.getId(), newTower, thisUploadShouldThrowIOException, t2Banner))
        .isInstanceOf(StorageException.class)
        .hasMessageContaining("banner or icon");

    // Verify the StorageService methods were called an expected number of times
    verify(storageService, atMost(3)).saveTowerIcon(any(), any(), any());
    verify(storageService, never()).saveTowerBanner(any(), any(), any());
  }
}
