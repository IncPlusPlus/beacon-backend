package io.github.incplusplus.beacon.centralidentityserver.persistence.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
  @Id private String id;
  private String emailAddress;
  private String username;
  private String password;
  private String profilePictureUrl;
}
