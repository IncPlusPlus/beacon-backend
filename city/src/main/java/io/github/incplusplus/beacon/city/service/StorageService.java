package io.github.incplusplus.beacon.city.service;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

  /**
   * Upload a file to the storage service and get a user-accessible URL for that attachment.
   *
   * @param file the file to upload
   * @param towerId the ID of the tower the file was uploaded to
   * @param channelId the ID of the channel the file was sent in
   * @param senderId the ID of the user who sent the file
   * @return a URL that points to the newly-uploaded file
   * @throws IOException if the file couldn't be uploaded
   */
  String saveUserAttachment(MultipartFile file, String towerId, String channelId, String senderId)
      throws IOException;

  /**
   * Upload an image to be used as the icon for a tower.
   *
   * @param icon a PNG file
   * @param towerId the ID of the tower
   * @return a URL that points to the newly-uploaded file
   * @throws IOException if the file couldn't be uploaded
   */
  String saveTowerIcon(MultipartFile icon, String towerId) throws IOException;

  /**
   * Upload an image to be used as the banner for a tower.
   *
   * @param banner a PNG file
   * @param towerId the ID of the tower
   * @return a URL that points to the newly-uploaded file
   * @throws IOException if the file couldn't be uploaded
   */
  String saveTowerBanner(MultipartFile banner, String towerId) throws IOException;
}
