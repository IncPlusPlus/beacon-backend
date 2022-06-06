package io.github.incplusplus.beacon.city.persistence.model;

import java.time.Instant;
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
public class Message {
  @Id private String id;
  private String channelId;
  private String towerId;
  private String senderId;
  private Instant sentTime;
  private String messageBody;
  private List<String> attachments;
  /** This is true if the message was edited (changed after it was sent) */
  private boolean edited;
}
