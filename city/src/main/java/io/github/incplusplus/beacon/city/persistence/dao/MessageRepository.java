package io.github.incplusplus.beacon.city.persistence.dao;

import io.github.incplusplus.beacon.city.persistence.model.Message;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface MessageRepository extends CrudRepository<Message, String> {

  List<Message> findAllByTowerIdAndChannelId(String towerId, String channelId);
}
