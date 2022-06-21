package io.github.incplusplus.beacon.city.persistence.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Document
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Tower {
  @Id private String id;
  private String name;
  private String adminAccountId;
  private List<String> moderatorAccountIds;
  private List<String> memberAccountIds;
  private String iconUrl;
  private String bannerUrl;
  private String primaryColor;
  private String secondaryColor;

  @ReadOnlyProperty
  @DocumentReference(lookup = "{'towerId':?#{#self._id} }")
  private List<Channel> channels;
}
