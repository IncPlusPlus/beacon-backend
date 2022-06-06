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
  }

  @Override
  public String save(MultipartFile file, String towerId, String channelId, String senderId)
      throws IOException {
    // This isn't exactly the smartest way to use a TSID, but it's good enough for now.
    long tsid = TsidCreator.getTsid256().toLong();

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(file.getInputStream().available());
    if (file.getContentType() != null && !"".equals(file.getContentType())) {
      metadata.setContentType(file.getContentType());
    }
    String fileKey =
        towerId + "/" + channelId + "/" + tsid + "/" + file.getOriginalFilename();
    s3Client.putObject(
        new PutObjectRequest(props.getBucket(), fileKey, file.getInputStream(), metadata)
            .withCannedAcl(CannedAccessControlList.PublicRead));

    return getFileEdgeUrl(fileKey);
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
        + ".cdn.digitaloceanspaces.com/"
        + fileKey;
  }
}
