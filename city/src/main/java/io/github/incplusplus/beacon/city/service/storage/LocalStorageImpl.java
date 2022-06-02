package io.github.incplusplus.beacon.city.service.storage;

import com.github.f4b6a3.tsid.TsidCreator;
import io.github.incplusplus.beacon.city.properties.LocalStorageProperties;
import io.github.incplusplus.beacon.city.service.StorageService;
import io.github.incplusplus.beacon.city.spring.AutoRegisterCity;
import io.github.incplusplus.beacon.common.exception.StorageException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** The default storage provider for user-uploaded content. */
@Service
@ConditionalOnMissingBean(value = StorageService.class, ignored = LocalStorageImpl.class)
public class LocalStorageImpl implements StorageService {

  private final Path rootLocation;

  private final AutoRegisterCity autoRegisterCity;

  @Autowired
  public LocalStorageImpl(LocalStorageProperties properties, AutoRegisterCity autoRegisterCity) {
    this.rootLocation = Paths.get(properties.getLocation());
    this.autoRegisterCity = autoRegisterCity;
  }

  @Override
  public String save(MultipartFile file, String towerId, String channelId, String senderId) {
    // This isn't exactly the smartest way to use a TSID, but it's good enough for now.
    long tsid = TsidCreator.getTsid256().toLong();
    Path attachmentPath =
        Paths.get(towerId, channelId, Long.toString(tsid), file.getOriginalFilename());
    try {
      if (file.isEmpty()) {
        throw new StorageException("Failed to store empty file.");
      }
      Path destinationFile =
          // TODO: It might be wise to do some really basic name checking to find malicious chars
          // like ".", "..", etc.
          this.rootLocation.resolve(attachmentPath).normalize().toAbsolutePath();
      if (!destinationFile.startsWith(this.rootLocation.toAbsolutePath())) {
        // This is a security check
        throw new StorageException("Cannot store file outside current directory.");
      }
      try (InputStream inputStream = file.getInputStream()) {
        // Create necessary directories (or copy op will fail with NoSuchFileException)
        // https://stackoverflow.com/a/2833883/1687436
        //noinspection ResultOfMethodCallIgnored
        destinationFile.toFile().getParentFile().mkdirs();
        Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException e) {
      throw new StorageException("Failed to store file.", e);
    }
    // Switch this to http if you're testing locally
    return "http://"
        + autoRegisterCity.getHostName()
        // Add this part if you're testing locally
        + ":8080"
        // I know this sucks. Shut up.
        + "/attachments/"
        + attachmentPath.toString().replace("\\", "/");
  }
}
