package io.github.incplusplus.beacon.city.persistence.dao;

import io.github.incplusplus.beacon.city.persistence.model.Message;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface MessageRepository extends CrudRepository<Message, String> {

  void deleteAllByChannelId(String channelId);

  Optional<Message> deleteMessageById(String id);

  List<Message> findAllByTowerIdAndChannelId(String towerId, String channelId);
}
