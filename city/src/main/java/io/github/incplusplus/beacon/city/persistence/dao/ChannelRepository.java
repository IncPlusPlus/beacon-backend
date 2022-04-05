package io.github.incplusplus.beacon.city.persistence.dao;

import io.github.incplusplus.beacon.city.persistence.model.Channel;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface ChannelRepository extends CrudRepository<Channel, String> {
  Optional<Channel> deleteChannelById(String id);
}
