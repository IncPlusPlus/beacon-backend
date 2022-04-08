package io.github.incplusplus.beacon.centralidentityserver.persistence.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

@Document
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TowerInvite {
  @Id private String id;
  private String inviteCode;
  private String inviter;
  private String towerId;
  private String cityId;
  private Instant dateCreated;
  private int uses;
  private int maxUses;
  @Nullable private Instant expiryDate;
  private boolean revoked;
}
