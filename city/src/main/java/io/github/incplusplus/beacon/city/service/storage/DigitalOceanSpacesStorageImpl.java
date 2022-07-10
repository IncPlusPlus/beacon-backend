package io.github.incplusplus.beacon.city.service.storage;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.github.f4b6a3.tsid.TsidCreator;
import io.github.incplusplus.beacon.city.properties.DigitalOceanStorageProperties;
import io.github.incplusplus.beacon.city.service.StorageService;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @see <a
 *     href="https://thepro.io/post/spring-boot-and-digitalocean-spaces-for-file-storage-Q1">Spring
 *     Boot and DigitalOcean Spaces for File Storage</a>
 */
@Service
@ConditionalOnProperty(prefix = "do.spaces", value = "key")
public class DigitalOceanSpacesStorageImpl implements StorageService {
  private final Pattern bannerOrIconUrlPattern;
  private final DigitalOceanStorageProperties props;

  private final AmazonS3 s3Client;

  public DigitalOceanSpacesStorageImpl(@Autowired DigitalOceanStorageProperties props) {
    this.props = props;
    BasicAWSCredentials creds = new BasicAWSCredentials(props.getKey(), props.getSecret());
    this.s3Client =
        AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(
                new EndpointConfiguration(props.getEndpoint(), props.getRegion()))
            .withCredentials(new AWSStaticCredentialsProvider(creds))
            .build();
    // This is awful lol
    bannerOrIconUrlPattern =
        Pattern.compile(
            "("
                // Basically this gets the origin bucket URL
                + "https://"
                + props.getBucket()
                + "."
                + props.getEndpoint()
                + "/"
                // OR
                + "|"
                // the cdn URL
                + props.getBucket()
                + "."
                + props.getRegion()
                + ".cdn."
                + getDomainFromEndpoint()
                + "/"
                + ")"
                // followed by the elements of the file path
                + "([a-f\\d]+)/(icon|banner)-(\\d+)\\.png");
  }

  @Override
  public String saveUserAttachment(
      MultipartFile file, String towerId, String channelId, String senderId) throws IOException {
    // This isn't exactly the smartest way to use a TSID, but it's good enough for now.
    long tsid = TsidCreator.getTsid256().toLong();

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(file.getInputStream().available());
    if (file.getContentType() != null && !"".equals(file.getContentType())) {
      metadata.setContentType(file.getContentType());
    }
    String fileKey = towerId + "/" + channelId + "/" + tsid + "/" + file.getOriginalFilename();
    s3Client.putObject(
        new PutObjectRequest(props.getBucket(), fileKey, file.getInputStream(), metadata)
            .withCannedAcl(CannedAccessControlList.PublicRead));

    return getFileEdgeUrl(fileKey);
  }

  @Override
  public String saveTowerIcon(MultipartFile icon, String towerId, String existingUrl)
      throws IOException {
    return saveTowerIconOrBanner(icon, towerId, true, existingUrl);
  }

  @Override
  public String saveTowerBanner(MultipartFile banner, String towerId, String existingUrl)
      throws IOException {
    return saveTowerIconOrBanner(banner, towerId, false, existingUrl);
  }

  private String saveTowerIconOrBanner(
      MultipartFile file, String towerId, boolean isIcon, String existingUrl) throws IOException {
    // This isn't exactly the smartest way to use a TSID, but it's good enough for now.
    long tsid = TsidCreator.getTsid256().toLong();
    // Delete the existing file if it exists
    if (existingUrl != null && !existingUrl.isBlank()) {
      try {
        s3Client.deleteObject(props.getBucket(), getFileKeyForIconOrBannerUrl(existingUrl));
      } catch (IllegalStateException ignored) {
        // It may be the case that the URL hasn't been set yet. We can ignore this.
      }
    }

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(file.getInputStream().available());
    if (file.getContentType() != null && !"".equals(file.getContentType())) {
      metadata.setContentType(file.getContentType());
    }
    String fileKey = towerId + "/" + (isIcon ? "icon" : "banner") + "-" + tsid + ".png";
    s3Client.putObject(
        new PutObjectRequest(props.getBucket(), fileKey, file.getInputStream(), metadata)
            .withCannedAcl(CannedAccessControlList.PublicRead));

    return getIconOrBannerEdgeUrl(fileKey, tsid, isIcon);
  }

  private String getIconOrBannerOriginUrl(String towerId, long tsid, boolean isIcon) {
    // https://beaconcdn.nyc3.digitaloceanspaces.com/623de7d41b2b8c392b5e23d0/icon-12312321.png
    return "https://"
        + props.getBucket()
        + "."
        + props.getEndpoint()
        + "/"
        + towerId
        + "/"
        + (isIcon ? "icon" : "banner")
        + "-"
        + tsid
        + ".png";
  }

  private String getIconOrBannerEdgeUrl(String towerId, long tsid, boolean isIcon) {
    // https://beaconcdn.nyc3.cdn.digitaloceanspaces.com/623de7d41b2b8c392b5e23d0/banner-123241.png
    return "https://"
        + props.getBucket()
        + "."
        + props.getRegion()
        + ".cdn."
        + getDomainFromEndpoint()
        + "/"
        + towerId
        + "/"
        + (isIcon ? "icon" : "banner")
        + "-"
        + tsid
        + ".png";
  }

  private String getFileOriginUrl(String fileKey) {
    // https://beaconcdn.nyc3.digitaloceanspaces.com/623de7d41b2b8c392b5e23d0/624ca52e535e2e1cbf9f390a/321681041876494489/beacon-central-identity-server-bundled.openapi.yml
    return "https://" + props.getBucket() + "." + props.getEndpoint() + "/" + fileKey;
  }

  private String getFileEdgeUrl(String fileKey) {
    // https://beaconcdn.nyc3.cdn.digitaloceanspaces.com/623de7d41b2b8c392b5e23d0/624ca52e535e2e1cbf9f390a/321681041876494489/beacon-central-identity-server-bundled.openapi.yml
    return "https://"
        + props.getBucket()
        + "."
        + props.getRegion()
        + ".cdn."
        + getDomainFromEndpoint()
        + "/"
        + fileKey;
  }

  private String getDomainFromEndpoint() {
    return props.getEndpoint().substring(props.getEndpoint().indexOf("."));
  }

  private String getFileKeyForIconOrBannerUrl(String url) {
    Matcher matcher = bannerOrIconUrlPattern.matcher(url);
    if (!matcher.find()) {
      throw new IllegalStateException(
          "Icon or banner for url '" + url + "' doesn't seem quite right. Unable to delete it.");
    }
    return matcher.group(2) + "/" + matcher.group(3) + "-" + matcher.group(4) + ".png";
  }
}
