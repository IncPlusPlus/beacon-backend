package io.github.incplusplus.beacon.centralidentityserver.persistence.model;

import java.util.List;
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
public class City {
  @Id private String id;
  private String password;
  private String basePath;
  /**
   * This list contains the IDs of any users who are a member of one or more Towers that this City
   * contains. It is the responsibility of the City to add and remove users from this collection
   * when applicable. For example, if a user (who isn't a member of any Towers in this City) joins
   * one of the Towers this City contains. It is the City's responsibility to notify the CIS that
   * there is a new user who is a "member of the City".
   *
   * <p>The City obviously doesn't have members. The only membership mechanic in this application is
   * users being members of Towers. The "members of the City" is a concept that was created for the
   * express purpose of allowing the frontend to ask the <i>specifically the <b>CIS</b></i> "what
   * Towers am I a member of and what Cities do I need to ask to retrieve their information?" and is
   * not to be taken as a City having some kind of membership mechanic.
   */
  private List<String> memberUsers;
}
