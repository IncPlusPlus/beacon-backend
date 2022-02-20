package io.github.incplusplus.beacon.centralidentityserver.service;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * A slightly modified version of the StorageService interface from the linked Spring tutorial. This
 * interface associates stored objects with a user ID.
 *
 * @see <a href="https://spring.io/guides/gs/uploading-files/">Uploading Files</a>
 */
public interface StorageService {

  void init();

  void store(MultipartFile file, String userId);

  Stream<Path> loadAll();

  Path load(String filename);

  Resource loadAsResource(String filename);

  void deleteAll();
}
