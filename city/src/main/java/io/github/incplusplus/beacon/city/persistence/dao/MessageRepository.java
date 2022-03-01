package io.github.incplusplus.beacon.city.persistence.dao;

import io.github.incplusplus.beacon.city.persistence.model.Message;
import org.springframework.data.repository.CrudRepository;

public interface MessageRepository extends CrudRepository<Message, String> {}
