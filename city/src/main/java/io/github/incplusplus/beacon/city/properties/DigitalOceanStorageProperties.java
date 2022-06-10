package io.github.incplusplus.beacon.city.properties;

import javax.validation.constraints.NotBlank;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("do.spaces")
@Validated
@ConditionalOnProperty(
    prefix = "do.spaces",
    value = {"key", "secret"})
public class DigitalOceanStorageProperties {

  @NotBlank private String key;
  @NotBlank private String secret;
  @NotBlank private String endpoint;
  @NotBlank private String region;
  @NotBlank private String bucket;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }
}
